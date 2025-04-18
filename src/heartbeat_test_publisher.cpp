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

#include <chrono>
#include <memory>

#include "rclcpp/rclcpp.hpp"
#include "std_msgs/msg/int32.hpp"

using namespace std::chrono_literals;

int main(int argc, char* argv[]) {
  rclcpp::init(argc, argv);

  auto node = rclcpp::Node::make_shared("heartbeat_publisher");
  auto publisher =
      node->create_publisher<std_msgs::msg::Int32>("/heartbeat", 10);

  int count = 0;
  rclcpp::WallRate rate(1);  // Publish once per second

  while (rclcpp::ok()) {
    auto message = std_msgs::msg::Int32();
    message.data = count++;

    RCLCPP_INFO(node->get_logger(), "Publishing heartbeat: %d", message.data);
    publisher->publish(message);

    rate.sleep();
  }

  rclcpp::shutdown();
  return 0;
}