const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp();

exports.notifyNewClientRequest = functions.firestore
    .document('trainers/{trainer}/clientRequests/{client}')
    .onCreate((docSnapshot, context) => {
        const message = docSnapshot.data();
        const clientFirstName = message['first_name'];
        const clientLastName = message['last_name'];
        const recipientID = context.params.trainer;
        console.log(recipientID);

        return admin.firestore().doc('users/' + recipientID).get().then(userDoc => {
            const registrationToken = userDoc.get('token');
            console.log(userDoc);

            const payload = {
                notification: {
                    title: "Client Request",
                    body: clientFirstName + " " + clientLastName + " sent you a request!",
                    clickAction: "ClientRequestsActivity"
                }
            };
            return admin.messaging().sendToDevice(registrationToken, payload).then( response => {
                console.log("Successful notification sent", response)
                return 0;
            })
                .catch( error =>
                {
                    console.log("Error sending message", error)
                })
        })

    });

