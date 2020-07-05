From node:14
Workdir /app
Add . /app
RUN npm install
EXPOSE 3000
CMD npm start