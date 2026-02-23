import React from 'react';
import { MemoryRouter } from "react-router-dom";
import { render, screen } from "@testing-library/react";
import App from "./App";

test("renders LogIn page at '/'", () => {
    render(
        <MemoryRouter initialEntries={["/"]}>
            <App />
        </MemoryRouter>
    );

    expect(screen.getByText("LOGIN")).toBeInTheDocument();
});