@REQ_NIK-5
Feature: Slot Booking and Scheduling

	#Tests As a registered user, I want to check all reservations that I’ve made, so that I can verify if I already have a reservation.
	@TEST_NIK-385 @REQ_NIK-13
	Scenario: Test List reservation
		Given the following charging stations exist:
		  | name      | latitude | longitude | availability |
		  | Station 1 | 0.0      | 0.0       | FREE         |
		When I open the application
		And I am authenticated
		And the following reservations are booked:
          | startsAt  | endsAt   |
          | 50        | 51       |
          | 52        | 53       |
          | -22       | -20      |
          | -49       | -48      |
		 And I go to my profile
		 Then I should see 4 reservations
		 And reservation 1 starts at 50
		 And reservation 2 starts at 52
		 And reservation 3 starts at -22
		 And reservation 4 starts at -49
		
