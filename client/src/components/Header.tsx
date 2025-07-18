import { useLocation } from 'preact-iso';
import { routes } from '..';

export function Header() {
 const { url } = useLocation();

 return (
  <header>
   <nav>
    <a href={routes.knowledgeUnitManagmentPath()} class={url == routes.knowledgeUnitManagmentPath() && 'active'}>
     Knowledge Units
    </a>
    <a href={routes.documentsPath()} class={url == routes.documentsPath() && 'active'}>
     Documents
    </a>
    <a href="/404" class={url == '/404' && 'active'}>
     404
    </a>
   </nav>
  </header>
 );
}
