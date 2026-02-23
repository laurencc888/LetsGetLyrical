Feature: Compare favorite song list with friends
  Scenario: Go to Compare page from Search page after clicking on Compare tab
    Given I am on the Search page
    When I click the "Compare" tab on the navigation bar
    Then I should be on the "Compare" page

  Scenario: Go to Compare page from Favorites page after clicking on Compare tab
    Given I am on the Favorites page
    When I click the "Compare" tab on the navigation bar
    Then I should be on the "Compare" page

  Scenario: Go to Favorites page from Compare page after clicking on Favorites tab
    Given I am on the Compare page
    When I click the "Favorites" tab on the navigation bar
    Then I should be on the "Favorites" page

  Scenario: Go to Search page from Compare page after clicking on Search tab
    Given I am on the Compare page
    When I click the "Search" tab on the navigation bar
    Then I should be on the "Search" page

  Scenario: Error comparing favorites if other user(s)' favorites list is private
    Given I am on the Compare page
    And "TommyTrojan" is an existing user
    And "TommyTrojan"'s favorite songs are "Song Title 1", "Song Title 3"
    And "TommyTrojan"'s favorite list is private
    And "Jane Doe" is an existing user
    And "Jane Doe"'s favorite songs are "Song Title 1", "Song Title 3"
    And "Jane Doe"'s favorite list is public
    And I'm an existing user
    And I have favorites songs "Song Title 1", "Song Title 2"
    When I type in "TommyTrojan, Jane Doe" in the usernames field
    And I click the "Compare Lists" button
    Then I don't see a combined favorites list
    And the message "Error: One or more user’s favorite lists are private"

  Scenario: All valid other users shows combined favorites list
    Given I am on the Compare page
    And "TommyTrojan" is an existing user
    And "TommyTrojan"'s favorite songs are "Song Title 1", "Song Title 3"
    And "TommyTrojan"'s favorite list is public
    And "Jane Doe" is an existing user
    And "Jane Doe"'s favorite songs are "Song Title 1", "Song Title 3"
    And "Jane Doe"'s favorite list is public
    And I'm an existing user
    And I have favorites songs "Song Title 1", "Song Title 2"
    When I type in "TommyTrojan, Jane Doe" in the usernames field
    And I click the "Compare Lists" button
    Then I see a combined favorites list with "3" songs

  Scenario: Favorites list combination includes songs and their user count
    Given I am on the Compare page
    And "TommyTrojan", "Jane Doe" and I are existing users
    And "TommyTrojan"'s favorite songs are "Song Title 1", "Song Title 3"
    And "TommyTrojan"'s favorite list is public
    And "Jane Doe"'s favorite songs are "Song Title 1", "Song Title 3"
    And "Jane Doe"'s favorite list is public
    And I have favorites songs "Song Title 1", "Song Title 2"
    When I type in "TommyTrojan, Jane Doe" in the usernames field
    And I click the "Compare Lists" button
    Then I see a combined favorites list with "3" songs
    And the first song is "Song Title 1" with frequency "3"
    And the second song is "Song Title 3" with frequency "2"
    And the third song is "Song Title 2" with frequency "1"

    # can delete this scenario... more quality control
  Scenario: Error comparing favorites list with 0 other users
    Given I am on the Compare page
    And the usernames field is empty
    When I click the "Compare Lists" button
    Then I don't see a combined favorites list
    And I see the error "Please fill out this field."

  Scenario: Error comparing favorites list with 1 valid and 1 invalid other user
    Given I am on the Compare page
    And "TommyTrojan" is an existing user
    And "TommyTrojan"'s favorite songs are "Song Title 1"
    And "TommyTrojan"'s favorite list is public
    And I'm an existing user
    And I have favorite songs "Song Title 1", "Song Title 2"
    When I type in "TommyTrojan, Jane_Doe_DNE" in the usernames field
    And I click the "Compare Lists" button
    Then I don't see a combined favorites list
    And the error "Error: one or more usernames do not exist"

  Scenario: Clicking favorites combination song shows title, artist, year
    Given I am on the Compare page
    And "TommyTrojan" is existing user with favorite song "Song Title 1"
    And "TommyTrojan"'s favorite list is public
    And I'm an existing user
    And I have favorite songs "Song Title 1", "Song Title 2"
    And I type in "TommyTrojan" in the usernames field
    And I click the "Compare Lists" button
    And I see a combined favorites list with "2" songs
    And the first song is "Song Title 1" with frequency "2"
    And the second song is "Song Title 2" with frequency "1"
    When I click on "Song Title 2"
    Then I see "Song Title 2"
    And I see Song Title 2's Artist and Recording Year

  Scenario: Combined favorites list sorts songs most to least common by default
    Given I am on the Compare page
    And "TommyTrojan" is an existing user
    And "TommyTrojan"'s favorite songs are "Song Title 1"
    And "TommyTrojan"'s favorite list is public
    And I'm an existing user
    And I have favorite songs "Song Title 1", "Song Title 2"
    And I type in "TommyTrojan" in the usernames field
    When I click the "Compare Lists" button
    Then I see a combined favorites list with "2" songs
    And the first song is "Song Title 1" with frequency "2"
    And the second song is "Song Title 2" with frequency "1"

  Scenario: Click combo favorites button sorts songs to least to most common
    Given I am on the Compare page
    And "TommyTrojan" is an existing user with favorite song "Song Title 1"
    And "TommyTrojan"'s favorite list is public
    And I am an existing user with favorites "Song Title 1" and "Song Title 2"
    And I type "TommyTrojan" in the usernames field
    And I click the "Compare Lists" button
    Then I see a combined list with "2" songs
    And the first song is "Song Title 1" with frequency "2"
    And the second song is "Song Title 2" with frequency "1"
    When I click the "Least to Most Frequent Favorite Song" button
    Then I see the "2" songs, first song as "Song Title 2" with frequency "1"
    And the second song as "Song Title 1" with frequency "2"

    # again this is more so quality control
  Scenario: No favorite songs in common, a favorites list combo is shown
    Given I am on the Compare page
    And "TommyTrojan" is existing user and favorite song "Song Title 1"
    And "TommyTrojan"'s favorite list is public
    And "Jane Doe" is an existing user and favorite songs "Song Title 2"
    And "Jane Doe"'s favorite list is public
    And I'm an existing user with favorites songs "Song Title 3"
    When I type in "TommyTrojan, Jane Doe" in the usernames field
    And I click the "Compare Lists" button
    Then I see a combined favorites list with "3" songs
    And the first song is "Song Title 1" with frequency "1"
    And the second song is "Song Title 2" with frequency "1"
    And the third song is "Song Title 3" with frequency "1"

  Scenario: click/hover favorite song rate shows users who favorite it
    Given I am on the Compare page
    And "TommyTrojan" is an existing user
    And "TommyTrojan" with favorites "Song Title 1", "Song Title 3"
    And "Jane Doe" is an existing user
    And "Jane Doe" with favorites "Song Title 1", "Song Title 3"
    And I'm existing user with favorite "Song Title 1", "Song Title 2"
    And "Jane Doe"'s and "TommyTrojan"'s lists are public
    And I compare lists with "TommyTrojan" and "Jane Doe"
    And I see a combined favorites list with "3" songs
    And the first song is "Song Title 1" with frequency "3"
    And the second song is "Song Title 3" with frequency "2"
    And the third song is "Song Title 2" with frequency "1"
    When I click or hover over frequency "2" next to "Song Title 3"
    Then I should see a pop up with usernames "TommyTrojan", "Jane Doe"
