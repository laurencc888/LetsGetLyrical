import React from "react";
import {render, screen, waitFor} from "@testing-library/react";
import Register from "./Register";
import userEvent from "@testing-library/user-event";
import {MemoryRouter} from "react-router-dom";
import fetchMock from "jest-fetch-mock";
import Compare from "./Compare";

const usernameLabel = "Username";
const passwordLabel = "Password";
const confirmLabel = "Confirm Password";
const submitButtonLabel = "Submit";
const cancelButtonLabel = "Cancel";

function renderPage() {
    render(
        <MemoryRouter>
            <Register />
        </MemoryRouter>
    )
}

beforeAll(() => {
    fetchMock.enableMocks();
    window.alert = jest.fn();
});

beforeEach(() => {
    fetchMock.resetMocks();
    delete window.location;
    window.location = { href: "" };
    renderPage();
});

const enterInfo = async (user, username, password, confirm) => {
    await user.type(screen.getByPlaceholderText(usernameLabel), username);
    await user.type(screen.getByPlaceholderText(passwordLabel), password);
    await user.type(screen.getByPlaceholderText(confirmLabel), confirm);
};

const submitPass = async (user, username, password) => {
    await enterInfo(user, username, password, password);
    await waitFor(() => user.click(screen.getByText(submitButtonLabel)));
}

test("cancel returns to log in page", async () => {
    const user = userEvent.setup();
    window.confirm = jest.fn(() => true);

    await waitFor(() => user.click(screen.getByText(cancelButtonLabel)));
    expect(window.location.href).toBe("/login");
});

test("invalid passwords", async () => {
    const user = userEvent.setup();

    const username = "Username";
    await submitPass(user, username, "password123");
    expect(screen.getByText("1 Capital Letter")).toBeInTheDocument();
});

test("failed registration error", async () => {
    const user = userEvent.setup();
    fetch.mockResponseOnce("invalid registration", {
        status: 100,
    });

    await enterInfo(user, "User", "Password123", "Password123");
    await waitFor(() => user.click(screen.getByText(submitButtonLabel)));

    expect(screen.getByText("invalid registration")).toBeInTheDocument();
});

test("unexpected registration error", async () => {
    const user = userEvent.setup();
    fetch.mockResponseOnce(null, {
        status: 100,
    });

    await enterInfo(user, "User2", "Aa1", "Aa1");
    await waitFor(() => user.click(screen.getByText(submitButtonLabel)));

    expect(screen.getByText("An unexpected error occurred.")).toBeInTheDocument();
});

jest.setTimeout(1000);
test("successfully register a new user and reveal password", async () => {
    const user = userEvent.setup();
    fetch.mockResponseOnce("valid registration", {
        status: 200,
    });

    await enterInfo(user, "User", "Password123", "Password123");
    await waitFor(() => user.click(screen.getByTestId("toggle-password")));
    await waitFor(() => user.click(screen.getByTestId("toggle-confirm-password")));

    await waitFor(() => user.click(screen.getByText(submitButtonLabel)));
    await waitFor(() => {
        expect(screen.getByText("Registration successful! You can now log in.")).toBeInTheDocument();
    })
    await waitFor(() => {
        expect(window.alert).toHaveBeenCalledWith("Registration successful! Redirecting to login page...");
    });
});

jest.setTimeout(70000)
test("redirect user if timed out on register", async () => {
    delete window.location; // delete the default location object
    window.location = { href: '' }; // assign mock object

    Object.defineProperty(window, "localStorage", {
        value: {
            getItem: jest.fn(() => "mockUser"),
            setItem: jest.fn(),
            removeItem: jest.fn(),
        },
        writable: true,
    });

    renderPage();

    await new Promise(resolve => setTimeout(resolve, 60000));

    expect(window.location.href).toBe('/');
})