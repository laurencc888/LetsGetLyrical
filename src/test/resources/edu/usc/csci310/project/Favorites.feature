Feature: Favorite page functionality
  Scenario: Basic rendering and navigation of favorites page
    Given I am registered as "admin1" and "Pw123" and on the Login page
    And I am logged in on Search page
    When I click the "Favorites" tab on the navigation bar
    Then I should be on the Favorites page
    And I should see a list of favorite songs

  Scenario: Hovering over a song shows arrows
    Given I am registered as "admin2" and "Pw123" and on the Login page
    And I am on the Favorites page and there are 1 favorite songs
    When I hover over favorite song number 1
    Then I should see the up arrow button
    And I should see the down arrow button

  Scenario: Deleting a song shows confirmation message
    Given I am registered as "admin3" and "Pw123" and on the Login page
    And I am on the Favorites page and there are 1 favorite songs
    When I hover over favorite song number 1
    And I click the delete button for favorite song number 1
    Then I should see the delete confirmation message

  Scenario: Deleting all song shows confirmation message
    Given I am registered as "admin4" and "Pw123" and on the Login page
    And I am on the Favorites page and there are 1 favorite songs
    When I click the delete all button
    Then I should see the delete all confirmation message

  Scenario: Moving a song up
    Given I am registered as "admin5" and "Pw123" and on the Login page
    And I am on the Favorites page and there are 2 favorite songs
    When I hover over favorite song number 2
    And I click the up arrow for favorite song number 2
    Then I should see the song move to position 1

  Scenario: Moving a song down
    Given I am registered as "admin6" and "Pw123" and on the Login page
    And I am on the Favorites page and there are 2 favorite songs
    When I hover over favorite song number 1
    And I click the down arrow for favorite song number 1
    Then I should see the song move to position 2

  Scenario: Deleting a song with confirmation
    Given I am registered as "admin7" and "Pw123" and on the Login page
    And I am on the Favorites page and there are 1 favorite songs
    When I hover over favorite song number 1
    And I click the delete button for favorite song number 1
    And I click yes in the delete confirmation message
    Then The song should be deleted and not shown anymore

  Scenario: Deleting a song without confirmation
    Given I am registered as "admin8" and "Pw123" and on the Login page
    And I am on the Favorites page and there are 1 favorite songs
    When I hover over favorite song number 1
    And I click the delete button for favorite song number 1
    Then I should see the delete confirmation message
    And I click cancel in the delete confirmation message
    And The song is not deleted and still shown

  Scenario: Deleting all songs with confirmation
    Given I am registered as "admin9" and "Pw123" and on the Login page
    And I am on the Favorites page and there are 1 favorite songs
    When I click the delete all button
    Then I should see the delete all confirmation message
    And I click yes in the delete all confirmation message
    And I see the message "No Favorite Songs"

  Scenario: Deleting all songs without confirmation
    Given I am registered as "admin10" and "Pw123" and on the Login page
    And I am on the Favorites page and there are 1 favorite songs
    When I click the delete all button
    Then I should see the delete all confirmation message
    And I click cancel in the delete all confirmation message
    And No songs are delete and all are still shown

  Scenario: Viewing song information
    Given I am registered as "admin11" and "Pw123" and on the Login page
    And I am on the Favorites page and there are 1 favorite songs
    When I hover over favorite song number 1
    And I click the song title for favorite song number 1
    Then I see a pop up with the song title, artist, and year of recording

  Scenario: Maintaining song order
    Given I am registered as "admin12" and "Pw123" and on the Login page
    And I am on the Favorites page and there are 2 favorite songs
    When I hover over favorite song number 2
    And I click the up arrow for favorite song number 2
    And I refresh the page
    Then I should see the song move to position 1

  Scenario: User is private by default
    Given I am registered as "admin13" and "Pw123" and on the Login page
    And I am logged in on Search page
    When I click the "Favorites" tab on the navigation bar
    Then I should be on the Favorites page
    And I should see that the private selection is toggled

  Scenario: Maintaining user privacy status
    Given I am registered as "admin14" and "Pw123" and on the Login page
    And I am on the Favorites page
    When I toggle the public selection
    And I refresh the page
    Then I should see that the user is public

  Scenario: Toggling between private and public button
    Given I am registered as "admin15" and "Pw123" and on the Login page
    And I am on the Favorites page
    When I toggle the public selection
    Then I should see that the private button is not toggled
    When I toggle the private selection
    Then I should see that the public button is not toggled
