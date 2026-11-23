import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import router from '@/router';
import '@/styles/variables.css';
import '@/styles/global.css';
import '@/styles/header.css';   // ← 추가
import '@/styles/footer.css';   // ← 추가

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>,
);
