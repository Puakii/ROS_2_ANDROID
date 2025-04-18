// Copyright 2016 Open Source Robotics Foundation, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#include <arpa/inet.h>
#include <netdb.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>

#include <chrono>
#include <iostream>
#include <memory>
#include <string>

#include "rclcpp/rclcpp.hpp"
#include "std_msgs/msg/int32.hpp"

using namespace std;
using namespace std::chrono_literals;

int clientSocket = -1;

void heartbeat_callback(const std_msgs::msg::Int32::SharedPtr msg,
                        rclcpp::Logger logger) {
  if (clientSocket == -1) {
    RCLCPP_WARN(logger, "No client connected to send heartbeat");
    return;
  }

  std::string message = std::to_string(msg->data) + "\n";
  ssize_t sent = send(clientSocket, message.c_str(), message.length(), 0);

  if (sent < 0) {
    RCLCPP_ERROR(logger, "Failed to send heartbeat, closing client socket");
    close(clientSocket);
    clientSocket = -1;
  } else {
    RCLCPP_INFO(logger, "Sent heartbeat: %s", message.c_str());
  }
}

int main(int argc, char *argv[]) {
  int listening = socket(AF_INET, SOCK_STREAM, 0);

  if (listening == -1) {
    cerr << "Can't create a socket! Quitting" << endl;
    return -1;
  }

  sockaddr_in hint;
  hint.sin_family = AF_INET;
  hint.sin_port = htons(5557);
  inet_pton(AF_INET, "0.0.0.0", &hint.sin_addr);

  int opt = 1;
  setsockopt(listening, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));

  if (bind(listening, (sockaddr *)&hint, sizeof(hint)) < 0) {
    cerr << "Bind failed!" << endl;
    return -1;
  }

  listen(listening, SOMAXCONN);
  cout << "[Server] Listening on port 5557..." << endl;

  sockaddr_in client;
  socklen_t clientSize = sizeof(client);
  clientSocket = accept(listening, (sockaddr *)&client, &clientSize);
  if (clientSocket < 0) {
    cerr << "Accept failed!" << endl;
    return -1;
  }

  char host[NI_MAXHOST];
  char service[NI_MAXSERV];
  memset(host, 0, NI_MAXHOST);
  memset(service, 0, NI_MAXSERV);
  close(listening);  // Stop accepting others

  rclcpp::init(argc, argv);
  auto node = rclcpp::Node::make_shared("heartbeat_server");

  auto sub = node->create_subscription<std_msgs::msg::Int32>(
      "/heartbeat", 10, [node](std_msgs::msg::Int32::SharedPtr msg) {
        heartbeat_callback(msg, node->get_logger());
      });

  rclcpp::spin(node);

  if (clientSocket != -1) {
    close(clientSocket);
  }

  rclcpp::shutdown();
  return 0;
}