@REQ_NIK-5
Feature: Slot Booking and Scheduling

	#As a registered user, I want to reserve a slot in a charging station, so that I can use it later
  @TEST_NIK-370 @REQ_NIK-12
  Scenario: Test Book a slot
	Given I have the account with email "test@test.com" and password "password"
    And the following charging stations exist:
      | name      | latitude | longitude | availability |
      | Station 1 | 0.0      | 0.0       | FREE         |
    When I open the application
    And I am authenticated
    And I browse for the closest station to 'Aveiro, Portugal'
    And I select a station
    And I select a charger to book
    Then A reservation dialog should appear
    And I fill the reservation time for tomorrow at noon for 1 hour
    Then I should get confirmation that the reservation was created
		
