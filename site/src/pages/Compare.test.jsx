import React from "react";
import {MemoryRouter} from "react-router-dom";
import Compare from "./Compare";
import {render, screen, waitFor} from "@testing-library/react";
import userEvent from "@testing-library/user-event";


const user = userEvent.setup();

function renderPage() {
    render(
        <MemoryRouter>
            <Compare />
        </MemoryRouter>
    )
}

// resetting mocking behavior
beforeEach(() => {
    jest.resetAllMocks();
    renderPage();
    localStorage.getItem = jest.fn().mockReturnValue("username");
});

const mockUserJson =
    {
        favorites: [
            {
                songId: 100,
                songName: "Blank Space",
                artistId: 1,
                artistName: "Taylor Swift",
                lyrics: "blah blah love...",
                releaseYear: 2014
            }
        ],
        isPublic: true,
        username: "username"
    };

const mockValidFriend =
    {
        favorites: [
            {
                songId: 100,
                songName: "Blank Space",
                artistId: 1,
                artistName: "Taylor Swift",
                lyrics: "blah blah love...",
                releaseYear: 2014
            },
            {
                songId: 101,
                songName: "Love Story",
                artistId: 1,
                artistName: "Taylor Swift",
                lyrics: "blah blah love...",
                releaseYear: 2013
            }
        ],
        isPublic: true,
        username: "valid"
    };

const mockPrivateFriend =
    {
        favorites: [
            {
                songId: 100,
                songName: "Blank Space",
                artistId: 1,
                artistName: "Taylor Swift",
                lyrics: "blah blah love...",
                releaseYear: 2014
            }
        ],
        isPublic: false,
        username: "private"
    };

test("successfully compare with a valid friend", async () => {
    global.fetch = jest.fn()
        .mockResolvedValueOnce({
            json: async () => mockValidFriend,
            ok: true,
        })
        .mockResolvedValueOnce({
            json: async () => mockUserJson,
            ok: true,
        });

    await user.type(screen.getByPlaceholderText("Input the username of friend(s)..."), "valid");
    await user.click(screen.getByText("Compare Lists"));

    await screen.findByLabelText("Most to Least Frequent Favorite Song");
    // changing sort order
    await user.click(screen.getByText("Least to Most Frequent Favorite Song"));
    await user.click(screen.getByText("Most to Least Frequent Favorite Song"));

    await user.click(screen.getByText("Blank Space"));
    expect(screen.getByText("Taylor Swift")).toBeInTheDocument();
    await user.click(screen.getByText("✖"));

    screen.getByText("Blank Space").focus();
    await user.keyboard('{Shift}');
    await user.keyboard('{Enter}');
    await user.click(screen.getByText("✖"));

    await user.click(screen.getByText("2"));
    expect(screen.getByText("Users who favorited this song")).toBeInTheDocument();
    await user.click(screen.getByText("✖"));


    screen.getByText("2").focus();
    await user.keyboard('{Shift}');
    await user.keyboard('{Enter}');
    await user.click(screen.getByText("✖"));

    // hovering feature
    await user.hover(screen.getByText("2"));
    await new Promise(resolve => setTimeout(resolve, 1100));
    expect(screen.getByText("Users who favorited this song")).toBeInTheDocument();
});

test("a user is private", async () => {
    global.fetch = jest.fn()
        .mockResolvedValueOnce({
            json: async () => mockPrivateFriend,
            ok: true,
        })
        .mockResolvedValueOnce({
            json: async () => mockUserJson,
            ok: true,
        });

    await user.type(screen.getByPlaceholderText("Input the username of friend(s)..."), "private");
    await user.click(screen.getByText("Compare Lists"));

    await screen.findByText(/Error comparing lists./i);
    expect(screen.getByText(/Error comparing lists./i)).toBeInTheDocument();
});

test("a user doesn't exist", async () => {
    global.fetch = jest.fn()
        .mockResolvedValueOnce({
            json: async () => null,
            ok: true,
        })
        .mockResolvedValueOnce({
            json: async () => mockUserJson,
            ok: true,
        });

    await user.type(screen.getByPlaceholderText("Input the username of friend(s)..."), "dne");
    await user.click(screen.getByText("Compare Lists"));

    await screen.findByText("Error comparing lists. One or more usernames do not exist.");
    expect(screen.getByText("Error comparing lists. One or more usernames do not exist.")).toBeInTheDocument();
});

test("user fetch causes error", async () => {
    global.fetch = jest.fn()
        .mockResolvedValueOnce({
            json: async () => mockUserJson,
            ok: false,
        })
        .mockResolvedValueOnce({
            json: async () => mockUserJson,
            ok: false,
        });

    await user.type(screen.getByPlaceholderText("Input the username of friend(s)..."), "dne");
    await user.click(screen.getByText("Compare Lists"));

    await screen.findByText("Error comparing lists. One or more usernames do not exist.");
    expect(screen.getByText("Error comparing lists. One or more usernames do not exist.")).toBeInTheDocument();
});

test("no user means go to login from compare page", async () => {
    Storage.prototype.getItem = jest.fn(() => null);

    delete window.location; // delete the default location object
    window.location = { href: '' }; // assign mock object

    Object.defineProperty(window, "localStorage", {
        value: {
            getItem: jest.fn(() => null),
            setItem: jest.fn(),
            removeItem: jest.fn(),
        },
        writable: true,
    });

    render(
        <MemoryRouter>
            <Compare/>
        </MemoryRouter>
    );
    expect(window.location.href).toBe('/');
});
