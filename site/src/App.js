import React from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import LogIn from './pages/LogIn';
import Register from './pages/Register'; // Import Register
import './styles/index.css';
import Search from './pages/Search';
import Favorites from './pages/Favorites';
import Compare from "./pages/Compare";


function App() {
    return (
        <div>
            <Routes>
                {/* When someone goes to /, show the Login page */}
                <Route path="/" element={<LogIn />} />

                {/* When someone goes to /register, show the Register page */}
                <Route path="/register" element={<Register />} />
                <Route path="/login" element={<LogIn/>} />
                <Route path="/search" element={<Search/>} />
                <Route path="/favorites" element={<Favorites/>} />
                <Route path="/compare" element={<Compare/>} />

                {/* Catch-all: redirect everything else back to / */}
                <Route path="*" element={<Navigate to="/" replace />} />

            </Routes>
        </div>
    );
}

export default App;