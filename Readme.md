# Example Spring-Boot application Internationalization

The application also shows how to upload an images to DB and show (download) those from DB.

Used technologies:

- Thymeleaf for web page rendering
- Spring JPA repository for Person entity storage in DB
- Spring controller: PersonController 

## Localized messages
For localized messages the files are used:

~~~
  messages.properties, messages_lv.properties
~~~

The configuration beans are defined in InternationalizationConfig.java file.
The SessionLocaleResolver is used.

## Validation messages and localization

For Validation messages the files are used:

~~~
   messages.properties, messages_lv.properties
~~~
   
Other files can be configured for use.

The configuration beans are defined in ValidationConfig.java file.    