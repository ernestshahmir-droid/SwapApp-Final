# 🤝 SwapApp - Peer-to-Peer Skill Exchange

SwapApp is a comprehensive native Android application designed to facilitate peer-to-peer service trading. Instead of using traditional currency, users exchange skills and services using a custom virtual time-credit system to promote community collaboration and resource sharing.

Developed as a final year software engineering dissertation project, this application was built using native Java and XML in Android Studio, strictly following an Agile development lifecycle.

## ✨ Core Features (Agile Epics)

* **🔐 Secure Authentication (Sprint 1):** User registration and secure login powered by **Firebase** to ensure user states and platform data remain protected.
* **⏳ Time-Credit Wallet (Sprint 2):** Custom transaction logic that securely tracks, deducts, and awards virtual time balances automatically upon task completion.
* **🛒 Skill Marketplace (Sprint 3):** Dynamic user interfaces allowing users to browse, publish, filter, and negotiate local skill exchanges.
* **📴 Offline Data Caching (Sprint 4):** Integrated **SQLite** local database persistence ensuring core functionalities, wallet balances, and profile details remain accessible without continuous internet access.
* **📝 Peer-to-Peer Requests (Sprint 5):** Request services from other users and manage ongoing exchange agreements, tracking earned versus spent time credits alongside peer feedback.

## 🛠️ Technology Stack

* **Front-End:** XML (Native Android UI)
* **Back-End:** Java (Android SDK)
* **Cloud Database & Infrastructure:** Google Firebase
* **Local Data Persistence:** SQLite Database
* **System Modeling:** UML (Class, Component, Use Case) & Data Flow Diagrams (DFD Levels 0, 1, 2)
* **Version Control:** Git & GitHub
* **Project Management:** Jira (Scrum Methodology, Sprint Backlogs, Epics, Burndown Charts)
* **Testing:** Zephyr Scale (Manual Execution & User Acceptance Testing)

## 📱 Application Architecture 
SwapApp utilizes standard Android Activity lifecycles within an MVC framework. Key architectural highlights include:
* Comprehensive system modeling and entity mapping using DFDs and UML structures to ensure robust scalability.
* Strict transaction verification logic to ensure users cannot spend more time-credits than they have currently earned.
* Robust offline data state management, seamlessly syncing local SQLite caches with Firebase infrastructure when network connectivity is restored.

## 🚀 How to Run Locally

1. Clone the repository:
   ```bash
   git clone [https://github.com/ernestshahmir-droid/SwapApp-Final.git](https://github.com/ernestshahmir-droid/SwapApp-Final.git)
