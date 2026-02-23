import React from "react";
import {render, screen} from "@testing-library/react";
import {MemoryRouter} from "react-router-dom";
import ArtistPop from "./ArtistPop";
import userEvent from "@testing-library/user-event";

const artistJson = [
    {
        id: 1,
        name: "Taylor Swift",
        imageUrl: "https://images.genius.com/df4a816f593b08bc8a361ad58a848640.1000x1000x1.jpg"
    }
];

function renderPage() {
    render(
        <MemoryRouter>
            <ArtistPop
                filteredResults={artistJson}
                onArtistSelect={jest.fn()}
                onClose={jest.fn()}
            />
        </MemoryRouter>
    );
}

test("Render Artist pop up and interact", async () => {
    const user = userEvent.setup();
    renderPage();

    await user.click(screen.getByTestId("artist-item-0"));
    expect(screen.getByText("Taylor Swift")).toBeInTheDocument();
});

test("close pop up", async () => {
    const user = userEvent.setup();
    renderPage();

    await user.click(screen.getByTestId("close-artist-pop"));
    expect(screen.getByText("Taylor Swift")).toBeInTheDocument();
});

test("select via tabbing", async () => {
   const user = userEvent.setup();
   renderPage();

    screen.getByTestId("artist-item-0").focus();
    await user.keyboard('{Shift}');

   screen.getByTestId("artist-item-0").focus();
   await user.keyboard('{Enter}');
   expect(screen.getByText("Taylor Swift")).toBeInTheDocument();
});