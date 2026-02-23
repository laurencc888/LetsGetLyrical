import React from "react";
import {render, screen, waitFor} from "@testing-library/react";
import Favorites from "./Favorites";
import userEvent from "@testing-library/user-event";
import {MemoryRouter} from "react-router-dom";

// mock localStorage
beforeAll(() => {
  Object.defineProperty(window, "localStorage", {
    value: {
      getItem: jest.fn(() => "mockUser"),
      setItem: jest.fn(),
      removeItem: jest.fn(),
    },
    writable: true,
  });
});

beforeEach(() => {
    window.localStorage.getItem = jest.fn(() => 'mockUser');

    global.fetch = jest.fn((url) => {
        if (url === '/get-soulmate') {
            return Promise.resolve({
                json: () =>
                    Promise.resolve({
                        name: "Test Soulmate",
                        songs: ["Soulmate Song 1", "Soulmate Song 2"],
                    }),
            });
        }

        if (url === '/get-enemy') {
            return Promise.resolve({
                json: () =>
                    Promise.resolve({
                        name: "Test Enemy",
                        songs: ["Enemy Song 1", "Enemy Song 2"],
                    }),
            });
        }


        if (url.includes('/get-favorite-songs')) {
            return Promise.resolve({
                json: () =>
                    Promise.resolve([
                        {songName: "Mock Song 1", releaseYear: 2000},
                        {songName: "Mock Song 2", releaseYear: 2010},
                    ]),
            });
        }
    });
});


afterEach(() => {
  jest.restoreAllMocks(); // restores both fetch and localStorage
});

const mockMatchJson =
    {
        match: "true",
        name: "username",
        songs: ["yo"],
    }
;

const mockNullMatch =
    {
        match: null,
        name: "username",
        songs: ["yo"],
    }
;

const mockNoMatchJson =
    {
        match: "false",
        name: "username",
        songs: ["yo"],
    }
;

function renderPage() {
    render(
        <MemoryRouter>
            <Favorites />
        </MemoryRouter>
    )
}

test("click enemy and soulmate doesn't exist", async () => {
    const user = userEvent.setup();
    renderPage();

    await waitFor(() => user.click(screen.getByText("Enemy")));
    await waitFor(() => user.click(screen.getByText("Soulmate")));
    expect(screen.getByText("No Soulmate Found.")).toBeInTheDocument();
});

test("click enemy and soulmate successful match", async () => {
    const user = userEvent.setup();
    global.fetch = jest.fn()
        .mockResolvedValue({
            json: async () => mockMatchJson,
            ok: true,
        });
    renderPage();

    await waitFor(() => user.click(screen.getByText("Enemy")));
    await new Promise(res => setTimeout(res, 1500));
    await waitFor(() => user.click(screen.getByText("Soulmate")));
    await new Promise(res => setTimeout(res, 1500));
    expect(screen.getByText("username")).toBeInTheDocument();
});

test("click enemy and soulmate successful but no match", async () => {
    const user = userEvent.setup();
    global.fetch = jest.fn()
        .mockResolvedValue({
            json: async () => mockNoMatchJson,
            ok: true,
        });
    renderPage();

    await waitFor(() => user.click(screen.getByText("Enemy")));
    await new Promise(res => setTimeout(res, 1500));
    await waitFor(() => user.click(screen.getByText("Soulmate")));
    await new Promise(res => setTimeout(res, 1500));
    expect(screen.getByText("username")).toBeInTheDocument();

    await user.click(screen.getByText("✖"));
    expect(screen.queryByText("username")).not.toBeInTheDocument();
});

test("click enemy and soulmate match is null", async () => {
    const user = userEvent.setup();
    global.fetch = jest.fn()
        .mockResolvedValue({
            json: async () => mockNullMatch,
            ok: true,
        });
    renderPage();

    await waitFor(() => user.click(screen.getByText("Enemy")));
    await new Promise(res => setTimeout(res, 1500));
    expect(screen.queryByText("username")).toBeInTheDocument();
});

test("click enemy and soulmate successful but error", async () => {
    const user = userEvent.setup();
    global.fetch = jest.fn()
        .mockResolvedValue({
            json: async () => mockNoMatchJson,
            ok: false,
        });

    renderPage();

    await waitFor(() => user.click(screen.getByText("Enemy")));
    await new Promise(res => setTimeout(res, 1500));
    await waitFor(() => user.click(screen.getByText("Soulmate")));
    await new Promise(res => setTimeout(res, 1500));
    expect(screen.getByText("No Soulmate Found.")).toBeInTheDocument();
});

test("interact with songs", async () => {
  const user = userEvent.setup();
  renderPage();

  const targetSong = "Mock Song 1";
  await waitFor(() => screen.getByText(targetSong));

  await user.hover(screen.getByText(targetSong));
  await waitFor(() => user.click(screen.getByText("❌")));
  await waitFor(() => user.click(screen.getByText("Cancel")));

  await user.hover(screen.getByText(targetSong));
  await waitFor(() => user.click(screen.getByText("❌")));
  await waitFor(() => user.click(screen.getByText("Yes, delete")));

  expect(screen.queryByText(targetSong)).not.toBeInTheDocument();
});


test("visibility, moving songs, clearing all", async () => {
  const user = userEvent.setup();
  renderPage();

  const [firstSong, secondSong] = ["Mock Song 1", "Mock Song 2"];
  await waitFor(() => screen.getByText(firstSong));
  await waitFor(() => screen.getByText(secondSong));

  await user.hover(screen.getByText(secondSong));
  await waitFor(() => user.click(screen.getByText("⬆️")));

  await user.hover(screen.getByText(firstSong));
  await waitFor(() => user.click(screen.getByText("⬇️")));

  await waitFor(() => user.click(screen.getByText(firstSong)));
  await waitFor(() => user.click(screen.getByText("×")));

  await user.hover(screen.getByText(firstSong));
  await waitFor(() => user.click(screen.getByText("⬇️")));

  await user.hover(screen.getByText(firstSong));
  await waitFor(() => user.click(screen.getByText("⬆️")));

  await waitFor(() => user.click(screen.getByText("Public")));
  await waitFor(() => user.click(screen.getByText("Private")));

  await waitFor(() => user.click(screen.getByText("Delete All Songs")));
  await waitFor(() => user.click(screen.getByText("Cancel")));

  await waitFor(() => user.click(screen.getByText("Delete All Songs")));
  await waitFor(() => user.click(screen.getByText("Yes, delete all")));

  expect(screen.queryByText("No Favorite Songs")).toBeInTheDocument();
});

test("fetches and displays favorite songs", async () => {
  render(
    <MemoryRouter>
      <Favorites mockUsername="mockUser" />
    </MemoryRouter>
  );

  await waitFor(() => {
    expect(screen.getByText("Mock Song 1")).toBeInTheDocument();
    expect(screen.getByText("Mock Song 2")).toBeInTheDocument();
  });

  expect(fetch).toHaveBeenCalledWith("/get-favorite-songs?username=mockUser");
});

test("move-song backend is called correctly when reordering songs", async () => {
  const user = userEvent.setup();

  global.fetch = jest.fn((url, options) => {
    if (url.startsWith("/get-favorite-songs")) {
      return Promise.resolve({
        json: () =>
          Promise.resolve([
            { songId: 1, songName: "Mock Song 1", releaseYear: 2000 },
            { songId: 2, songName: "Mock Song 2", releaseYear: 2010 },
          ]),
      });
    }

    // ✅ Capture move-song POST request
    if (url === "/move-song" && options.method === "POST") {
      const body = JSON.parse(options.body);
      expect(["up", "down"]).toContain(body.direction);
      expect([1, 2]).toContain(body.songId);
      expect(body.username).toBe("mockUser");
    }

    return Promise.resolve({ json: () => Promise.resolve() });
  });

  render(
    <MemoryRouter>
      <Favorites mockUsername="mockUser" />
    </MemoryRouter>
  );

  await waitFor(() => screen.getByText("Mock Song 2"));
  await user.hover(screen.getByText("Mock Song 2"));
  await user.click(screen.getByText("⬆️"));

  await user.hover(screen.getByText("Mock Song 1"));
  await user.click(screen.getByText("⬇️"));

  expect(fetch).toHaveBeenCalledWith(expect.stringContaining("/move-song"), expect.any(Object));
});

test("failed to delete song", async () => {
    render(
        <MemoryRouter>
            <Favorites mockUsername="mockUser" />
        </MemoryRouter>
    );

    const user = userEvent.setup();

    await waitFor(() => {
        expect(screen.getByText("Mock Song 1")).toBeInTheDocument();
        expect(screen.getByText("Mock Song 2")).toBeInTheDocument();
    });
    global.fetch = jest.fn()
        .mockResolvedValueOnce({
            ok: false,
            text: async () => "Something went wrong"
        });


    await waitFor(() => user.click(screen.getByText("Mock Song 1")));
    await waitFor(() => user.click(screen.getByText("❌")));
    await waitFor(() => user.click(screen.getByText("Yes, delete")));

    await waitFor(() => {
        expect(screen.getByText("Mock Song 1")).toBeInTheDocument();
        expect(screen.getByText("Mock Song 2")).toBeInTheDocument();
    });
});

test("failed to delete all songs", async () => {
    render(
        <MemoryRouter>
            <Favorites mockUsername="mockUser" />
        </MemoryRouter>
    );

    const user = userEvent.setup();

    await waitFor(() => {
        expect(screen.getByText("Mock Song 1")).toBeInTheDocument();
        expect(screen.getByText("Mock Song 2")).toBeInTheDocument();
    });
    global.fetch = jest.fn()
        .mockResolvedValueOnce({
            json: async () => "blah",
            ok: false,
            text: async () => "Something went wrong"
        });

    await waitFor(() => user.click(screen.getByText("Delete All Songs")));
    await waitFor(() => user.click(screen.getByText("Yes, delete all")));
    await waitFor(() => {
        expect(screen.queryByText("Mock Song 1")).not.toBeInTheDocument();
    });
});

test("no user means go to login", async () => {
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
            <Favorites/>
        </MemoryRouter>
    );
    expect(window.location.href).toBe('/');
});

test("failed to fetch favorite songs", async () => {
    global.fetch = jest.fn().mockRejectedValue(new Error("fetching fav error"));

    render(
        <MemoryRouter>
            <Favorites mockUsername="mockUser" />
        </MemoryRouter>
    );
});

test("moving up and down", async () => {
    const user = userEvent.setup();
    renderPage();

    const [firstSong, secondSong] = ["Mock Song 1", "Mock Song 2"];
    await waitFor(() => screen.getByText(firstSong));
    await waitFor(() => screen.getByText(secondSong));

    await user.hover(screen.getByText(firstSong));
    await waitFor(() => user.click(screen.getByText("⬇️")));

    expect(screen.getByText(firstSong)).toBeInTheDocument();
});

jest.setTimeout(70000)
test("redirect user if timed out on favorites", async () => {
    const user = userEvent.setup();

    Storage.prototype.getItem = jest.fn(() => null);

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

    render(
        <MemoryRouter>
            <Favorites/>
        </MemoryRouter>
    );

    await new Promise(resolve => setTimeout(resolve, 60000));

    expect(window.location.href).toBe('/');
})