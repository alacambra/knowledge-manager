// Suppress all Ant Design CSS-in-JS warnings for Preact compatibility
if (typeof window !== 'undefined') {
 const originalErr = console.error;
 console.error = (...args) => {
  // Convert all arguments to strings to search through them
  const message = args.map(arg =>
   typeof arg === 'string' ? arg :
    typeof arg === 'object' ? JSON.stringify(arg) :
     String(arg)
  ).join(' ');

  // Block all Ant Design CSS-in-JS related warnings
  if (message.includes('Ant Design CSS-in-JS') ||
   message.includes('cleanup function after unmount') ||
   message.includes('cssinjs') ||
   message.includes('You are registering a cleanup function')) {
   return;
  }

  originalErr.apply(console, args);
 };
}

import { render } from 'preact';
import { options } from 'preact';
import { LocationProvider, Router, Route } from 'preact-iso';
import { StyleProvider } from '@ant-design/cssinjs';
import { ConfigProvider } from 'antd';

import { Header } from './components/Header.jsx';
import { NotFound } from './pages/_404.jsx';
import { DocumentsManagementPage } from './pages/DocumentsManagementPage.js';
import { KnowledgeUnitEdit } from './components/KnowledgeUnitForm.js';
import './style.css';
import 'antd/dist/reset.css';
import { KnowledgeUnitList } from './components/KnowledgeUnitList';
import { DocumentList } from './components/DocumentList';

// Configure Preact to align with browser's idle time
options.debounceRendering = (typeof requestIdleCallback !== 'undefined')
 ? requestIdleCallback
 : (cb) => setTimeout(cb, 16);


// Route path utilities
export const routes = {
 rootPath: "/",
 rootPattern: "/",
 editKuPath: (id: string) => `/knowledge-unit/${id}`,
 editKuPattern: '/knowledge-unit/:id',
 createKuPath: () => `/create-knowledge-unit/`,
 createKuPattern: `/create-knowledge-unit/`,
 knowledgeUnitManagmentPath: () => '/knowledge-units',
 knowledgeUnitManagmentPattern: '/knowledge-units',
 documentsPath: () => '/documents',
 documentsPattern: '/documents',
 createDocumentPath: () => '/create-document',
 createDocumentPattern: '/create-document',
};

export function App() {
 return (
  <StyleProvider hashPriority="high">
   <ConfigProvider
    theme={{
     cssVar: true, // Enable CSS variables
     hashed: false, // Disable hashed class names
    }}
   >
    <LocationProvider>
     <Header />
     <main>
      <Router>
       <Route path="/" component={KnowledgeUnitList} />
       <Route path={routes.editKuPattern} component={KnowledgeUnitEdit} />
       <Route path={routes.createKuPattern} component={KnowledgeUnitEdit} />
       <Route path={routes.knowledgeUnitManagmentPattern} component={KnowledgeUnitList} />
       <Route path={routes.documentsPattern} component={DocumentList} />
       <Route path={routes.createDocumentPattern} component={DocumentsManagementPage} />
       <Route default component={NotFound} />
      </Router>
     </main>
    </LocationProvider>
   </ConfigProvider>
  </StyleProvider>
 );
}

render(<App />, document.getElementById('app'));