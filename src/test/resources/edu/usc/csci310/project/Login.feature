Feature: Login and app security
  Scenario: "Let's Get Lyrical" title is visible on page
    Given I am on the Login page
    Then I should see the title "Let's Get Lyrical"

  Scenario: Sign Up tab is visible on Login page
    Given I am on the Login page
    Then I should see the "Sign Up" tab on the navigation bar

  Scenario: The user first opens the web app
    Given I am on the browser
    When I type in "http://localhost:8080" in the address bar
    Then I should be on the "LOGIN" page

  Scenario: The user navigates to the Sign Up page from the Login page
    Given I am on the Login page
    When I click the "Sign Up" tab on the navigation bar
    Then I should be on the "SIGN UP" page

  Scenario: The user navigates to the Login page from the Sign Up page
    Given I am on the Sign Up page
    When I click the "Login" tab on the navigation bar
    Then I should be on the "LOGIN" page

  Scenario: The user successfully logs in with the correct username and password
    Given I am on the Login page
    And I already registered with username "Jane Doe" and password "1Trojan"
    When I enter in the username "Jane Doe"
    And I enter in the password "1Trojan"
    And I click the "Submit" button
    Then I see message "Login successful! Redirecting to search..."
    And I should be on the "Search" page

  Scenario: The user forgets to input a username
    Given I am on the Login page
    When I enter in the password "1Trojan"
    And I click the "Submit" button
    Then I see the error "Please fill out this field."

  Scenario: The user forgets to input a password
    Given I am on the Login page
    When I enter in the username "Jane Doe"
    And I click the "Submit" button
    Then I see the error "Please fill out this field."

  Scenario: The user enters the wrong password
    Given I am on the Login page
    And I already registered with username "Jane Doe" and password "1Trojan"
    When I enter in the username "Jane Doe"
    And I enter in the password "2Trojan"
    And I click the "Submit" button
    Then I see the error "Login incorrect. 1 failed attempt. Try again."

  Scenario: The user enters the wrong username
    Given I am on the Login page
    And I already registered with username "Jane Doe" and password "1Trojan"
    And I enter in the username "John Doe"
    And I enter in the password "1Trojan"
    And I click the "Submit" button
    Then I see the error "Login incorrect. 1 failed attempt. Try again."

  Scenario: 30 seconds no login after 3 consecutive wrong tries within 1 minute
    Given I am on the Login page
    And I already registered with username "Jane Doe" and password "1Trojan"
    When I enter in the username "John Doe"
    And I enter in the password "1Trojan"
    And I click the "Submit" button
    And I enter in the username "John Doe"
    And I enter in the password "2Trojan"
    And I click the "Submit" button
    And I enter in the username "Jane Doe"
    And I enter in the password "2Trojan"
    And I click the "Submit" button
    Then I see the error "3 wrong tries in 1 minute. No login for 30 seconds."

  Scenario: Login tries allowed after 3 consecutive wrong tries more than 1 minute
    Given I am on the Login page
    And I already registered with username "Jane Doe" and password "1Trojan"
    And I enter in the username "John Doe"
    And I enter in the password "1Trojan"
    And I click the "Submit" button
    And I enter in the username "John Doe"
    And I enter in the password "2Trojan"
    And I click the "Submit" button
    And I wait 1 minute
    And I enter in the username "Jane Doe"
    And I enter in the password "2Trojan"
    And I click the "Submit" button
    Then I see the error "Login incorrect. 1 failed attempt. Try again."

  Scenario: No login with correct password and username because still locked out
    Given I am on the Login page
    And I already registered with username "Jane Doe" and password "1Trojan"
    And I see the error 3 wrong tries in 1 minute. No login for 30 seconds.
    When I enter in the username "Jane Doe"
    And I enter in the password "1Trojan"
    And I click the "Submit" button again
    Then I see the error "3 wrong tries in 1 minute. No login for 30 seconds."
