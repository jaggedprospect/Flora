# Flora

**Author : jaggedprospect**

Flora is an Android application that allows a user to select the optimal flower for a person based on a set of simple parameters. In addition, it has the ability to search for flower stores within the user's immediate area (or a specified location). 



## Requirements

- Android Studio (minimum API 26: Oreo)

- JDK 

  

## Installation

Since only the source code is provided, you will need to create a New Project in Android Studio and import the files from the **Flora** folder in the repository.

You will also need to connect a Firebase project to handle Google logins and to host a Firestore database.

This distribution is meant to demonstrate an early protoype for the final application. As a result, a moderate amount of modification will be required to get the app running. I hope to provide a better distribution in the near future.


## Usage

Upon loading the app, you will be presented with the Flora logo and a Google SignIn button. You must select a Google Account and sign in to continue.

After successfully signing in, you will be presented with a simple tabbed menu:

**1) Picking a Flower**
Press the "Get Started" button. You will be directed to a new activity.

From here, you can select any search parameters that you wish in order to find the flower that best suits your needs. If you do not wish to use one of the parameters, simply leave the spinner on "not selected." At least one parameter must be selected in order for a query to be made to the database. 

Currently, the Firestore database containing the Flower collection is not operational -- no search results will be displayed.

**2) Finding a Store**
Press the "Get Started" button. You will be presented with a simple activity with two buttons labeled as follows: "Use Current Location" and "Select A Location."

Selecting the former will determine you current location and display it with a marker on a Google Map in the next activity. 

Selecting the latter will direct you to a new activity. Here, you can enter an address that you will would like to locate. 

After pressing the button labeled "Finish," your entered location will be displayed in the same manner as above. 

Additionally, there is a Floating Action Button in the bottom right-hand corner on the Main Activity. This functions as a Settings button. It is not yet operational -- when completed, it will allow the user to modify the app themes, clear stored data, logout of their associated Google Account, and edit app text variables.



## Author Notes

There are still lots of additions and fixes to be made to Flora! I am continuing to work on it in the hopes of having a polished and fully functional app. The project in its current state is meant to function as a basic demo of what the app could be like. Much of the functionality, such as the Google Places API used for searching for flower shops, has not been implemented. 

I am continuing to work on Flora, and I hope to be able to publish an official distribution in the near future!



## Contributing

This project is not currently open to outside contributions.



## License

*Refer to the GitHub repository for this project : [Flora Repo](https://github.com/jaggedprospect/Flora)*
