import React from "react";
import {render, screen, waitFor} from "@testing-library/react";
import {MemoryRouter} from "react-router-dom";
import userEvent from "@testing-library/user-event";
import Register from "../pages/Register";

function renderRegister() {
    render(
        <MemoryRouter>
            <Register />
        </MemoryRouter>
    );
}

test("can render error and success message", async() => {
    const user = userEvent.setup();
    renderRegister();

    await user.type(screen.getByPlaceholderText("Username"), "User");
    await user.type(screen.getByPlaceholderText("Password"), "Password123");
    await user.type(screen.getByPlaceholderText("Confirm Password"), "Password12")
    await waitFor(() => user.click(screen.getByText("Submit")));
    expect(screen.getByText("Passwords do not match.")).toBeInTheDocument();

    // to close pop up
    await waitFor(() => user.click(screen.getByTestId("close-popup")));
    expect(screen.queryByText("Passwords do not match.")).not.toBeInTheDocument();

    await user.type(screen.getByTestId("confirm-password-id"), "Password123");
    await waitFor(() => user.click(screen.getByText("Submit")));
});