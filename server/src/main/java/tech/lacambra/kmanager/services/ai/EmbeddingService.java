package tech.lacambra.kmanager.services.ai;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@ApplicationScoped
public class EmbeddingService {

    private static final Logger LOGGER = Logger.getLogger(EmbeddingService.class.getName());

    // @ConfigProperty(name = "embedding.model.path")
    String modelPath;

    private OrtSession session;
    private HuggingFaceTokenizer tokenizer;
    private OrtEnvironment env;

    @Inject
    public EmbeddingService(@ConfigProperty(name = "embedding.model.path") String modelPath) {
        this.modelPath = modelPath;
    }

    @PostConstruct
    public void init() {
        try {
            // Initialize ONNX Runtime environment
            env = OrtEnvironment.getEnvironment();

            // Load ONNX model
            OrtSession.SessionOptions opts = new OrtSession.SessionOptions();
            session = env.createSession(modelPath + "/model.onnx", opts);

            // Load tokenizer
            tokenizer = HuggingFaceTokenizer.newInstance(Paths.get(modelPath + "/../tokenizer.json"));

            LOGGER.info("Embedding service initialized successfully");
        } catch (Exception e) {
            LOGGER.severe("Failed to initialize embedding service: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Generate embeddings for the given text
     */
    public float[] generateEmbedding(String text) {
        try {
            // Tokenize input text
            Encoding encoding = tokenizer.encode(text);
            long[] inputIds = encoding.getIds();
            long[] attentionMask = encoding.getAttentionMask();

            // Create token_type_ids (all zeros for single sentence)
            long[] tokenTypeIds = new long[inputIds.length];
            // Arrays are initialized with zeros by default in Java, but being explicit:
            java.util.Arrays.fill(tokenTypeIds, 0L);

            // Prepare input tensors
            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put("input_ids", OnnxTensor.createTensor(env, new long[][] { inputIds }));
            inputs.put("attention_mask", OnnxTensor.createTensor(env, new long[][] { attentionMask }));
            inputs.put("token_type_ids", OnnxTensor.createTensor(env, new long[][] { tokenTypeIds }));

            // Run inference
            try (OrtSession.Result result = session.run(inputs)) {
                // Get the last hidden state (usually the first output)
                float[][][] lastHiddenState = (float[][][]) result.get(0).getValue();

                // Mean pooling to get sentence embedding
                return meanPooling(lastHiddenState[0], attentionMask);
            }

        } catch (Exception e) {
            LOGGER.severe("Error generating embedding: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Perform mean pooling on token embeddings
     */
    private float[] meanPooling(float[][] tokenEmbeddings, long[] attentionMask) {
        int sequenceLength = tokenEmbeddings.length;
        int embeddingDim = tokenEmbeddings[0].length;
        float[] pooled = new float[embeddingDim];

        int validTokens = 0;
        for (int i = 0; i < sequenceLength; i++) {
            if (attentionMask[i] == 1) {
                validTokens++;
                for (int j = 0; j < embeddingDim; j++) {
                    pooled[j] += tokenEmbeddings[i][j];
                }
            }
        }

        // Average the embeddings
        for (int i = 0; i < embeddingDim; i++) {
            pooled[i] /= validTokens;
        }

        return pooled;
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (session != null) {
                session.close();
            }
            if (env != null) {
                env.close();
            }
            if (tokenizer != null) {
                tokenizer.close();
            }
        } catch (Exception e) {
            LOGGER.warning("Error during cleanup: " + e.getMessage());
        }
    }
}