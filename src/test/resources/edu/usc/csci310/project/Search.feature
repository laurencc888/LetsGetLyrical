@search
Feature: User can search and generate word cloud
  # (12 + 1)
  Scenario: Successfully search by "Popularity"
    Given I am on the Search page
    When I enter in the Artist "Taylor"
    And I click the "Get Artists" button
    And I click on "Taylor Swift" in Artist Results
    And I select "Popularity" from the selection menu
    And I input 2 for number of songs
    And I click the "Search" button
    Then There should be a word cloud with Taylor Swift's 2 most popular songs

  Scenario: List of artists related to search term with picture
    Given I am on the Search page
    When I enter in the Artist "Taylor"
    And I click the "Get Artists" button
    Then There should be a list of artists with names related to Taylor
    And There should be picture of each artist next to their name in the list

  # (12 + 1)
  Scenario: Successfully search by "Manual"
    Given I am on the Search page
    When I enter in the Artist "Taylor"
    And I click the "Get Artists" button
    And I click on "Taylor Swift" in Artist Results
    And I select "Manual" from the selection menu
    And I input 2 for number of songs
    And I click the "Get Songs" button
    And I select 2 songs from the song list
    And I click the "Search" button
    Then There should be a word cloud with Taylor Swift's 2 songs chosen

  # (2)
  Scenario: Error message when artist does not exist
    Given I am on the Search page
    When I enter in the Artist "HDHFSDHF"
    And I click the "Get Artists" button
    Then There should be error message "No artists found"

  # (2)
  Scenario: Error message when trying to Add to Results with no word cloud
    Given I am on the Search page
    And There is no word cloud on the page
    When I enter in the Artist "Taylor"
    And I click the "Get Artists" button
    And I click on "Taylor Swift" in Artist Results
    And I select "Popularity" from the selection menu
    And I input 5 for number of songs
    And I click the "Add to Results" button
    Then There should be error message "Need a valid word cloud!"

    # have an error message for manual too (2)
  Scenario: Error message when empty number of songs in manual search
    Given I am on the Search page
    When I enter in the Artist "Taylor"
    And I click the "Get Artists" button
    And I click on "Taylor Swift" in Artist Results
    And I select "Manual" from the selection menu
    And I leave # Songs empty
    And I click the "Search" button
    Then There should be error message "Number of songs is empty!"

  # didn't select any songs but has fields for manual --> error message (2)
  Scenario: Error message when songs not selected from list in manual search
    Given I am on the Search page
    When I enter in the Artist "Taylor"
    And I click the "Get Artists" button
    And I click on "Taylor Swift" in Artist Results
    And I select "Manual" from the selection menu
    And I input 5 for number of songs
    And I don't select songs
    And I click the "Search" button
    Then There should be error message "Please select songs from the list!"

  # (2)
  Scenario: Error message when empty number of songs in search by popularity
    Given I am on the Search page
    When I enter in the Artist "Taylor"
    And I click the "Get Artists" button
    And I click on "Taylor Swift" in Artist Results
    And I select "Popularity" from the selection menu
    And I leave # Songs empty
    And I click the "Search" button
    Then There should be error message "Number of songs is empty!"

# (2)
  Scenario: Error message when artist field is empty
    Given I am on the Search page
    When I leave Artist blank
    And I select "Popularity" from the selection menu
    And I input 5 for number of songs
    And I click the "Search" button
    Then There should be error message "Artist field is empty!"

  # (5 + 1)
  Scenario: Add on to word cloud using Add to Results
    Given I am on the Search page
    And There's already word cloud with "Taylor Swift"'s 2 most popular songs
    When I enter in the Artist "Tate M"
    And I click the "Get Artists" button
    And I click on "Tate McRae" in Artist Results
    And I select "Popularity" from the selection menu
    And I input 2 for number of songs
    And I click the "Add to Results" button
    Then Taylor Swift and Tate McRae's 2 most popular songs are in word cloud

  Scenario: Generate word cloud from Favorites
    Given I am on the Search page
    And "22" by "Taylor Swift" is in my Favorites
    And "chaotic" by "Tate McRae" is in my Favorites
    When I click the "Generate From Favorites" button
    Then There should be a word cloud with "22" and "chaotic" lyrics
    
  Scenario: Add to word cloud from Favorites
    Given I am logged in on the Search page
    And "22" by "Taylor Swift" is in my Favorites
    And "chaotic" by "Tate McRae" is in my Favorites
    And There is already a word cloud with "Taylor Swift"'s "Afterglow"
    When I click the "Add From Favorites" button
    Then Word cloud with "22" "chaotic" and "Afterglow" lyrics
    