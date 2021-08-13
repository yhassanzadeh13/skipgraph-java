lint:
	@mvn checkstyle:checkstyle
test:
	@mvn clean install
	@mvn test