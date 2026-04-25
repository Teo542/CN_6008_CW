import { initializeApp } from "https://www.gstatic.com/firebasejs/10.12.0/firebase-app.js";
import { getAuth } from "https://www.gstatic.com/firebasejs/10.12.0/firebase-auth.js";
import { getFirestore } from "https://www.gstatic.com/firebasejs/10.12.0/firebase-firestore.js";

const firebaseConfig = {
  apiKey: "AIzaSyAGkZSS3rEwcRWzrtGEYx9lB57n5tTy2-k",
  authDomain: "cityfix-e37a4.firebaseapp.com",
  projectId: "cityfix-e37a4",
  storageBucket: "cityfix-e37a4.firebasestorage.app",
  messagingSenderId: "424569539914"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getFirestore(app);
