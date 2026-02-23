import React from "react";
import {render, screen, waitFor} from "@testing-library/react";
import LogIn from "../pages/LogIn";
import {MemoryRouter} from "react-router-dom";

test("renders without crashing", async () => {
    render(
        <MemoryRouter>
            <LogIn />
        </MemoryRouter>
    );
    expect(screen.getByText("LOGIN")).toBeInTheDocument();
});