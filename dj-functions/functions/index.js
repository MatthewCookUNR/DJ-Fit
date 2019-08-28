const functions = require('firebase-functions');
const firebase_tools = require('firebase-tools');
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
                console.log("Successful notification sent", response);
                return 0;
            })
                .catch( error =>
                {
                    console.log("Error sending message", error)
                })
        })

    });

/**
 * Initiate a recursive delete of documents at a given path.
 *
 * The calling user must be authenticated and have the custom "admin" attribute
 * set to true on the auth token.
 *
 * This delete is NOT an atomic operation and it's possible
 * that it may fail after only deleting some documents.
 *
 * @param {string} data.path the document or collection path to delete.
 */
exports.recursiveDeleteTrainer = functions
    .runWith({
        timeoutSeconds: 540,
        memory: '2GB'
    })
    .https.onCall((data, context) => {
        const userID = data.userID;
        // Only allow admin users to execute this function.
        if ((context.auth.uid !== userID)) {
            throw new functions.https.HttpsError(
                'permission-denied',
                'Must be be owner to delete.'
            );
        }

        const path = "trainers/" + userID;
        console.log(
            `User ${context.auth.uid} has requested to delete path ${path}`
        );

        // Run a recursive delete on the given document or collection path.
        // The 'token' must be set in the functions config, and can be generated
        // at the command line by running 'firebase login:ci'.
        return firebase_tools.firestore
            .delete(path, {
                project: process.env.GCLOUD_PROJECT,
                recursive: true,
                yes: true,
            })
            .then(() => {
                return {
                    path: path
                };
            });
    });

