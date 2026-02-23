import React from "react";
import {render, screen, waitFor} from "@testing-library/react";
import {MemoryRouter} from "react-router-dom";
import userEvent from "@testing-library/user-event";
import SongWordCloud from "./SongWordCloud";

jest.mock('react-d3-cloud', () => () => <div data-testid="mocked-word-cloud" />);

const user = userEvent.setup();

const mockNoSongsJson = [
    {
        songName: 'Blank Space',
        songId: 100,
        artistId: 1,
        lyrics: '',
        releaseYear: 2025,
    }
]

const mockTwoSongsJson = [
    {
        songName: 'Blank Space',
        songId: 100,
        artistId: 1,
        lyrics: 'blah blank space blank space love love love love love combat specific walked stop stopped baked bake walk running I walking furries craving oboe roses firms',
        releaseYear: 2025,
    },
    {
        songName: 'Love Story',
        songId: 101,
        artistId: 1,
        lyrics: 'blah blah love love story love space blank blank space love',
        releaseYear: 2025,
    }
];

function renderPage() {
    render(
        <MemoryRouter>
            <SongWordCloud artist={"Taylor Swift"}  wordCloudData={mockTwoSongsJson}/>
        </MemoryRouter>
    )
}

function renderEmptyPage() {
    render(
        <MemoryRouter>
            <SongWordCloud artist={"Taylor Swift"} wordCloudData={[]}/>
        </MemoryRouter>
    )
}

beforeEach(() => {
    jest.resetAllMocks();
});


test("renders song details and lyrics modal through table", async () => {
    renderPage();

    await user.click(screen.getByText("Word Cloud"));
    await user.click(screen.getByText("Table"));
    await user.click(screen.getByText("specific"));
    await user.click(screen.getByText(/Blank Space/i));
    // stop propagation
    await user.click(screen.getByText("2025"));

    expect(screen.getByText("2025")).toBeInTheDocument();

    await user.click(screen.getByText("Close"));
    expect(screen.queryByText("Close")).not.toBeInTheDocument();
});

test("can tab and press enter in table", async () => {
    renderPage();

    await user.click(screen.getByText("Word Cloud"));
    await user.click(screen.getByText("Table"));

    screen.getByText("specific").focus();
    await user.keyboard('{Shift}');

    screen.getByText("specific").focus();
    await user.keyboard('{Enter}');

    screen.getByText(/Blank Space/i).focus();
    await user.keyboard('{Shift}');

    screen.getByText(/Blank Space/i).focus();
    await user.keyboard('{Enter}');
    // stop propagation
    await user.click(screen.getByText("2025"));

    expect(screen.getByText("2025")).toBeInTheDocument();

    await user.click(screen.getByText("Close"));
    expect(screen.queryByText("Close")).not.toBeInTheDocument();
});

test("renders nothing if no words", async () => {
   renderEmptyPage();

   expect(screen.queryByText("No songs to display.")).toBeInTheDocument();
});

test("adding to favorites", async () => {
    global.fetch = jest.fn()
        .mockResolvedValueOnce({
            ok: true,
            text: async () => "successfully added to database"
        })
        .mockResolvedValueOnce({
            ok: false,
            text: async () => "song already in database"
        })
        .mockResolvedValueOnce({
            ok: true
        })
        .mockResolvedValueOnce({
            ok: false,
            text: async () => "error adding song"
        });

    renderPage();

    await user.click(screen.getByText("Table"));
    await user.click(screen.getByText("specific"));
    await user.hover(screen.getByText(/Blank Space/i));
    await user.click(screen.getByTestId("add-to-favorites"));
    await user.click(screen.getByTestId("add-to-favorites"));
    await user.click(screen.getByTestId("add-to-favorites"));
    await user.click(screen.getByTestId("add-to-favorites"));

    expect(screen.getByText("Error adding song to favorites.")).toBeInTheDocument();
});