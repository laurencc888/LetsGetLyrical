Feature: Interacting/adding to Favorites with word cloud
  # (4 + 1)
  Scenario: Open table that lists words and their frequencies
    Given I am on the Search page
    When I enter in the Artist "Taylor"
    And I click the "Get Artists" button
    And I click on "Taylor Swift" in Artist Results
    And I select "Popularity" from the selection menu
    And I input 2 for number of songs
    And I click the "Search" button
    And I see a word cloud with Taylor Swift's 2 most popular songs
    And I click the "Table" button
    And I see the table of words
    And I see "i" and "you" as the top words in the table
    Then There should be a table of common words in her songs

  # (6 + 1)
  Scenario: Song list pops up when clicking in the word cloud
    Given I am on the Search page
    When I enter in the Artist "Taylor"
    And I click the "Get Artists" button
    And I click on "Taylor Swift" in Artist Results
    And I select "Popularity" from the selection menu
    And I input 2 for number of songs
    And I click the "Search" button
    And I click on a word such as "remember"
    Then There is a list of songs that "remember" comes from in the modal
    And The modal lists the frequency of "remember" in each song

  Scenario: Size of word correlates to frequency
    Given I am on the Search page
    When There already word cloud with "Taylor Swift"'s 2 most popular songs
    Then The more common a word is, the bigger the font

  Scenario: filler words are filtered out
    Given I am on the Search page
    When There already word cloud with "Taylor Swift"'s 2 most popular songs
    Then Filler words should not be part of the word cloud

  Scenario: varying tenses of a word are grouped together
    Given I am on the Search page
    When There already word cloud with "Taylor Swift"'s 2 most popular songs
    And There is "walked" in the lyrics
    And There is "walk" in the lyrics
    Then Only "walk" should appear in the cloud and not "walked"

    # just make sure we have enough words
  Scenario: the word cloud does not show more than 100 words
    Given I am on the Search page
    When There already word cloud with "Taylor Swift"'s 2 most popular songs
    Then There are only 100 different words displayed

  Scenario: the word cloud must render in less than 15 seconds
    Given I am on the Search page
    And I want to search for Taylor Swift's 2 most popular songs
    When I click the "Search" button
    Then There word cloud of Taylor Swift's 2 most popular songs in 15 seconds

  # (8 + 1)
  Scenario: Generate song details
    Given I am on the Search page
    When I enter in the Artist "Taylor"
    And I click the "Get Artists" button
    And I click on "Taylor Swift" in Artist Results
    And I select "Popularity" from the selection menu
    And I input 2 for number of songs
    And I click the "Search" button
    And I click on a word such as "remember"
    And I click on the song "All Too Well (10 Minute Version) (Taylor’s Version) [Live Acoustic]" in the list
    Then A popup should display title "All Too Well (10 Minute Version) (Taylor’s Version) [Live Acoustic]"
    And Artist should be listed as "Taylor Swift"
    And The popup should show the year recorded as "2021"
    And The lyrics should be displayed directly below the title
    And The word "remember" should be highlighted in the lyrics

  # (4 + 1)
  Scenario: Successfully add a song to Favorites
    Given I am on the Search page as "USER1"
    When I enter in the Artist "Taylor"
    And I click the "Get Artists" button
    And I click on "Taylor Swift" in Artist Results
    And I select "Popularity" from the selection menu
    And I input 2 for number of songs
    And I click the "Search" button
    And I click on a word such as "remember"
    And I hover over the song "All Too Well (10 Minute Version) (Taylor’s Version) [Live Acoustic]" in the list
    And I click the "♥ Add to Favorites!" button to add it to favorites
    Then I see confirmation message "Successfully added to favorites."

  # (2)
  Scenario: Unsuccessfully add a song to Favorites
    Given I am on the Search page as "USER2"
    And "All Too Well (10 Minute Version) (Taylor’s Version) [Live Acoustic]" by "Taylor Swift" is in my Favorites
    When I enter in the Artist "Taylor"
    And I click the "Get Artists" button
    And I click on "Taylor Swift" in Artist Results
    And I select "Popularity" from the selection menu
    And I input 2 for number of songs
    And I click the "Search" button
    And I click on a word such as "remember"
    And I hover over the song "All Too Well (10 Minute Version) (Taylor’s Version) [Live Acoustic]" in the list
    And I click the "♥ Add to Favorites!" button to add it to favorites
    Then There should be error message "Song is already in favorites."

  Scenario: Toggles back to word cloud correctly
    Given I am on the Search page as "USER3"
    And There is already a word cloud with "Taylor Swift"'s "22"
    When I click the "Table" button
    And I click the "Word Cloud" button
    Then There should be a word cloud with Taylor Swift's 22
