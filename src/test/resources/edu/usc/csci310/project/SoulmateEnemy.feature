Feature: soulmate and enemy in favorites page functionality
  Scenario: Valid Soulmate match found
    Given There are 3 users registered as "admin16", "admin17", and "admin18"
    And "admin16" and "admin18" are both public
    And "admin17" and "admin18" have the same songs
    And I am registered as "admin18" and "Pw123" and on the Login page
    When I am on the Favorites page and there are 2 favorite songs
    And I click the soulmate button
    Then I see the message "Your Lyrical Soulmate is:"
    And I should see the username "admin17" as my lyrical soulmate
    And I should see their favorites list underneath

  Scenario: Valid Enemy match found
    Given There are 3 users registered as "admin19", "admin20", and "admin21"
    And "admin19" and "admin20" are both public
    And "admin19" and "admin21" have no similar songs
    And I am registered as "admin21" and "Pw123" and on the Login page
    When I am on the Favorites page and there are 2 favorite songs
    And I click the enemy button
    Then I see the message "Your Lyrical Enemy is:"
    And I should see the username "admin19" as my lyrical enemy
    And I should see their favorites list underneath

  Scenario: Check positive animation pops up when soulmates match
    Given There is a public user registered as "admin22" and "Pw123"
    And They have 1 favorite song from "Taylor Swift"
    And I am registered as "admin23" and "Pw123" and on the Login page
    And I am on the Favorites page and there are 1 favorite songs
    And I toggle the public selection
    When I click the soulmate button
    Then I should see a thumbs up emoji pop up for a second

  Scenario: Check negative animation pops up when enemies match
    Given There is a public user registered as "admin24" and "Pw123"
    And They have 1 favorite song from "Lorde"
    And I am registered as "admin25" and "Pw123" and on the Login page
    And I am on the Favorites page and there are 1 favorite songs
    And I toggle the public selection
    When I click the enemy button
    Then I should see a thumbs down emoji pop up for a second
