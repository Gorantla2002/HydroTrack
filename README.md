HydrotrackPlus
HydroTrack Plus is a fully featured android app to allow users to log their daily water, protein and calorie consumption, providing a user-friendly interface to aid users in reaching their individual daily goal through personalisation of the user's daily goal based on their body weight and activity level, along with various gamification methods to provide motivation to maintain healthy habits. The app also has features of real time synchronisation to the cloud, offline access, customisable reminders and a variety of statistic displays and other android specific design and functionality features that have been implemented with a focus on modern android app development practices and a high degree of attention to user privacy.
HydroTrack Plus was developed as part of the CIS4034-N Mobile Application Development module at Teesside University (Student ID: S3548263). This project demonstrated a developers ability in the use of Kotlin, Jetpack Compose, MVVM Architecture and Firebase Integration.
Key Features
Logging Consumption: Users can easily record their water, protein and calorie consumption using the apps quick add buttons and custom amount fields. All logged consumption will be shown on the main dashboard of the app in an animated bar chart format.
User Customisable Daily Goal: Based on the user’s body weight and activity level, the app will automatically determine the user’s daily goal for each of the three types of consumption. In addition, users will have the option to manually adjust these values if they wish.
Gamification: The app uses achievement badges and streak tracking to help motivate the user to reach their daily goal. The app will reward the user for achieving certain milestones, providing encouragement to continue maintaining healthy habits.
Detailed Statistics Display: The app allows the user to view detailed historical records of their consumption including the ability to navigate between months and view interactive charts in the statistics display.

Real Time Cloud Sync and Offline Access: All user data will be synced in real time to the cloud and stored locally on the user’s device for offline access. This is accomplished using Firebase Cloud Firestore which provides both real time data syncing and local data storage when there is no internet connection available.
Customisable Reminders: The app allows the user to customise when they want to receive reminders about hydrating and consuming nutrients. These reminders are sent using Firebase Cloud Messaging and the WorkManager class, allowing the app to run in the background to send the reminders even when the app is closed. The reminders will also respect the user's quiet hours and follow the new permission requirements of Android 13 and later versions.
Dark Mode and Accessibility Options: The app includes full dark mode support utilising the Material 3 theme. The theme is dynamic and will persist across app sessions.
High Level of User Privacy: To protect the user’s privacy, the app does not include any third party analytics or advertisements. The app will only request the necessary permissions from the user (internet and optional notification).
Technical Stack
•	Language: Kotlin
•	UI: 100% Jetpack Compose
•	Architecture: Clean MVVM Architecture
•	Back End: Firebase Authentication, Cloud Firestore, and Firebase Cloud Messaging
•	Navigation: Jetpack Navigation Compose
•	Minimum SDK Version: API 24 (Android 7.0)
•	Target SDK Version: API 34 (Android 14)
Setting Up and Running the App
To set up and run the app, please complete the following steps:
1.	Clone or unzip the project.
2.	Open the project in Android Studio Hedgehog (2023.1.1) or later version.
3.	Create a Firebase project with Authentication, Firestore, and Cloud Messaging enabled.
4.	Download the ‘google-services.json’ file created during the Firebase setup and place it in the ‘app/’ directory.
5. Sync the Gradle project and rebuild the app.
6. Apply the provided Firestore Security Rules to isolate user data.
7. Run the app on either a virtual device or physical device running API 24 or later.
Project Layout
The project layout follows the standard Android layout with separate components to maintain a clean separation of concerns:
•	Data Models and Repositories
•	UI Screens organised by feature (e.g., auth, home, profile, settings, history, statistics, achievements)
•	Utilities for Notifications and Background Tasks
•	Modern Theme Implementation
Documentation and Demonstration Videos
A collection of screen recordings demonstrating all major features of the app is in Zip file. Additionally, documentation of the Scrum-based sprint development process used to develop the app is in the same directory. Lastly, a presentation slide deck used to present the app to a group of people is in zip file.
All the above items demonstrate the developer’s understanding of how to meet the requirements of the CIS4034-N Mobile Applications Development Module.

