import React from "react";
import {render, screen, waitFor} from "@testing-library/react";
import LogIn from "./LogIn";
import userEvent from "@testing-library/user-event";
import {MemoryRouter} from "react-router-dom";
import fetchMock from 'jest-fetch-mock';

const usernameLabel = "Username";
const passwordLabel = "Password";
const submitButtonLabel = "Submit";

beforeAll(() => {
    fetchMock.enableMocks();
    window.alert = jest.fn();
});

beforeEach(() => {
    fetchMock.resetMocks();
});

function renderPage() {
    render(
        <MemoryRouter>
            <LogIn />
        </MemoryRouter>
    )
}

const submitLogIn = async (user, username, password) => {
    renderPage();

    await user.type(screen.getByPlaceholderText(usernameLabel), username);
    await user.type(screen.getByPlaceholderText(passwordLabel), password);
    await waitFor(() => user.click(screen.getByText(submitButtonLabel)));
};

jest.setTimeout(50000)
test("lock user out of logging in", async () => {
    const user = userEvent.setup();
    fetch.mockResponse("couldn't log in user", {
        status: 100,
    });

    await submitLogIn(user, "User1", "Password123");
    await waitFor(() => user.click(screen.getByText(submitButtonLabel)));
    await waitFor(() => user.click(screen.getByText(submitButtonLabel)));
    await waitFor(() => user.click(screen.getByText(submitButtonLabel)));

    await new Promise(resolve => setTimeout(resolve, 30000));

    await waitFor(() => {
        expect(screen.getByText("3 wrong tries in 1 minute. No login for 30 seconds.")).toBeInTheDocument();
    })
})

test("successfully log in an existing user", async () => {
    const user = userEvent.setup();
    fetch.mockResponseOnce("successfully logged in user", {
        status: 200,
    });

    await submitLogIn(user, "User1", "Password123");
    await waitFor(() => {
        expect(window.alert).toHaveBeenCalledWith("Login successful! Redirecting to search...");
    });
});

test("fields not filled out so error", async () => {
    const user = userEvent.setup();
    renderPage();

    await waitFor(() => user.click(screen.getByText(submitButtonLabel)));
    expect(screen.getByText("Please enter both username and password.")).toBeInTheDocument();
});