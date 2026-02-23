import React from "react";
import {render, screen, waitFor} from "@testing-library/react";
import {MemoryRouter} from "react-router-dom";
import Search from "../pages/Search";
import userEvent from "@testing-library/user-event";

function renderPage() {
    render(
        <MemoryRouter>
            <Search />
        </MemoryRouter>
    )
}

afterEach(() => {
    jest.restoreAllMocks(); // restores both fetch and localStorage
});

test("renders nav bar", async () => {
    renderPage();

    expect(screen.getByText("Favorites")).toBeInTheDocument();
});

test("removes username correctly", async () => {
    const user = userEvent.setup();

    let username = "mockUser";
    Object.defineProperty(window, "localStorage", {
        value: {
            getItem: jest.fn(() => username),
            removeItem: jest.fn(() => {username = null}),
        },
        writable: true,
    });

    renderPage();
    expect(screen.getByText("Log Out")).toBeInTheDocument();

    expect(window.localStorage.getItem("username")).toEqual("mockUser");

    await waitFor(() => user.click(screen.getByText("Log Out")));

    expect(window.localStorage.getItem("username")).toEqual(null);
});