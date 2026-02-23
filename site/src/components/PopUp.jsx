import { React } from 'react';
import '../styles/index.css';

function PopUp({showPopup, errorMessage, setShowPopup, isError}) {
    return (
        showPopup && (
            <div className={`fixed bottom-5 right-5 ${isError ? 'bg-red-500' : 'bg-green-600'} text-white p-4 rounded shadow-lg w-64`}>
                <div className="flex justify-between items-start">
                            <span aria-label="Pop up error message" className="font-bold mr-2">
                              {errorMessage}
                            </span>
                    <button
                        tabIndex = "0" aria-label="Close pop up" data-testid="close-popup"
                        className="text-white font-bold"
                        onClick={() => setShowPopup(false)}
                    >
                        X
                    </button>
                </div>
            </div>
        )
    );
}

export default PopUp;