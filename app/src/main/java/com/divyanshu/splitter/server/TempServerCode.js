import express from 'express';
import http from 'http';
import { WebSocketServer } from 'ws';

const app = express();
const PORT = 3000;
const server = http.createServer(app);
const wss = new WebSocketServer({ server });
const users = [];

app.get('/', (req, res) => {
  res.send('Hello from Splitter!');
});

server.listen(PORT, () => {
  console.log(`Express server running at http://localhost:${PORT}/`);
});

wss.on('connection', (ws) => {
  console.log('New client connected');

  ws.on('message', (message) => {
    const data = JSON.parse(message);

    console.log(data);

    const user = findUser(data.name);

    switch (data.type) {
      case 'store_user':
        if (user != null) {
          // user already exists
          ws.send(
            JSON.stringify({
              type: 'user_already_exists',
              data: data.name,
            })
          );
          console.log('User already existed returning');
          return;
        }

        const newUser = {
          name: data.name,
          conn: ws,
        };

        try {
          ws.send(
            JSON.stringify({
              type: 'user_stored',
              data: data.name,
            })
          );
          console.log('User added');
        } catch (error) {
          console.log('Error adding ->', error);
        }

        users.push(newUser);
        break;

      case 'start_transfer':
        let userToCall = findUser(data.target);

        if (userToCall) {
          ws.send(
            JSON.stringify({
              type: 'transfer_response',
              data: userToCall.name,
            })
          );
        } else {
          ws.send(
            JSON.stringify({
              type: 'transfer_response',
              data: null,
            })
          );
        }
        break;

      case 'create_offer':
        let userToReceiveOffer = findUser(data.target);

        if (userToReceiveOffer) {
          userToReceiveOffer.conn.send(
            JSON.stringify({
              type: 'offer_received',
              name: data.name,
              data: data.data.sdp,
            })
          );
        }
        break;

      case 'create_answer':
        let userToReceiveAnswer = findUser(data.target);

        if (userToReceiveAnswer) {
          userToReceiveAnswer.conn.send(
            JSON.stringify({
              type: 'answer_received',
              name: data.name,
              data: data.data.sdp,
            })
          );
        }
        break;

      case 'ice_candidate':
        let userToReceiveIceCandidate = findUser(data.target);

        if (userToReceiveIceCandidate) {
          userToReceiveIceCandidate.conn.send(
            JSON.stringify({
              type: 'ice_candidate',
              name: data.name,
              data: {
                sdpMLineIndex: data.data.sdpMLineIndex,
                sdpMid: data.data.sdpMid,
                sdpCandidate: data.data.sdpCandidate,
              },
            })
          );
        }
        break;
    }
  });

  ws.on('close', () => {
    users.forEach((user) => {
      if (user.conn === ws) {
        users.splice(users.indexOf(user), 1);
      }
    });
    console.log('Client disconnected');
  });

  ws.on('error', (err) => {
    console.log('Error on socket,', err);
  });
});

const findUser = (username) => {
  return users.find((user) => user.name === username);
};
