Feature: Creating a user account for the web app
  Scenario: "Let's Get Lyrical" title is visible on page
    Given I am on the Sign Up page
    Then I should see the title "Let's Get Lyrical"

  Scenario: Team 9 is visible on page
    Given I am on the Sign Up page
    Then I should see "Team 9" on the page

  Scenario: Login tab is visible on Sign Up page
    Given I am on the Sign Up page
    Then I should see the "Login" tab on the navigation bar

  Scenario: The user navigates to the Sign Up page from the Login page
    Given I am on the Login page
    When I click the "Sign Up" tab on the navigation bar
    Then I should be on the "SIGN UP" page

  Scenario: The user navigates to the Login page from the Sign Up page
    Given I am on the Sign Up page
    When I click the "Login" tab on the navigation bar
    Then I should be on the "LOGIN" page

  Scenario: User has account with unique username and valid password
    Given I am on the Sign Up page
    When I enter in the username "Jane Doe"
    And I enter in the password "1Trojan"
    And I enter in the password "1Trojan" to confirm the password
    And I click the "Submit" button
    Then I see message "Registration successful! Redirecting to login page..."
    And I should be on the "Login" page

  Scenario: The user tries to create account with a username that already exists
    Given I am on the Sign Up page
    And there is already another user with the username "Jane Doe"
    When I enter in the username "Jane Doe"
    And I enter in the password "1Trojan"
    And I enter in the password "1Trojan" to confirm the password
    And I click the "Submit" button
    Then see error "username already exists"

  Scenario: The user creates account with unique username and invalid password
    Given I am on the Sign Up page
    When I enter in the username "Jane Doe"
    And I enter in the password "123456"
    And I enter in the password "123456" to confirm the password
    And I click the "Submit" button
    Then see error "Password needs uppercase letter, lowercase letter, number."

  Scenario: User creates account with unique username, valid password, no confirm
    Given I am on the Sign Up page
    When I enter in the username "Jane Doe"
    And I enter in the password "1Trojan"
    And I click the "Submit" button
    Then see error "Please fill out this field."

  Scenario: User make account with unique username, valid password, wrong confirm
    Given I am on the Sign Up page
    When I enter in the username "Jane Doe"
    And I enter in the password "1Trojan"
    And I enter in the password "2Troja" to confirm the password
    And I click the "Submit" button
    Then see error "Passwords do not match."

  Scenario: The user forgets to input a username
    Given I am on the Sign Up page
    When I enter in the password "1Trojan"
    And I enter in the password "1Trojan" to confirm the password
    And I click the "Submit" button
    Then see error "Please fill out this field."

  Scenario: The user forgets to input a password
    Given I am on the Sign Up page
    When I enter in the username "Jane Doe"
    And I click the "Submit" button
    Then see error "Please fill out this field."

  Scenario: The user cancels their registration and sees confirmation page
    Given I am on the Sign Up page
    When I enter in the username "Jane Doe"
    And I enter in the password "1Trojan"
    And I enter in the password "1Trojan" to confirm the password
    And I click the "Cancel" button
    Then I see message "Are you sure you want to cancel?"

  Scenario: The user cancels their registration then continues register
    Given I am on the Sign Up page
    And I enter in the username "Jane Doe"
    And I enter in the password "1Trojan"
    And I enter in the password "1Trojan" to confirm the password
    And I click the "Cancel" button
    When I click the "Cancel" button on the pop up
    Then I should be on the "SIGN UP" page
    And I still see the username "Jane Doe"
    And I still see the password "1Trojan"
    And I still see the confirm password "1Trojan"

  Scenario: The user cancels their registration and goes back to Login
    Given I am on the Sign Up page
    And I enter in the username "Jane Doe"
    And I enter in the password "1Trojan"
    And I enter in the password "1Trojan" to confirm the password
    And I click the "Cancel" button
    When I click the Ok button on the pop up
    Then I should be on the "LOGIN" page
