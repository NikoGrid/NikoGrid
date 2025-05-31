@REQ_NIK-40
Feature: User Profile Management

	#Tests As a user, I want to register an account, so that I can use it to store information and make operations that require an account.
	@TEST_NIK-259 @REQ_NIK-41
	Scenario: Test Account register
		When I open the application
		And navigate to the register page
		Then I see a register form
		And when I input the email "test@test.test" and password "test1234"
		Then I get redirected to the login page
		
