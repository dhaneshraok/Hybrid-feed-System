🌐 Hybrid Social Feed System

This project is a simulation written in Java that demonstrates the Hybrid Fanout Architecture, a core pattern used by massive social networks (like X/Twitter, Instagram, and Facebook) to manage high-volume content delivery efficiently.

🚀 Project Summary

The core problem for large social platforms is balancing the speed of posting (Write Latency) against the speed of loading a personalized feed (Read Latency). The Hybrid Fanout system solves this by:

PUSHING (Fanout-on-Write): Posts from Regular Users are immediately sent to all followers' caches (fast read).

PULLING (Fanout-on-Read): Posts from Celebrity Users (high follower count) are not pushed. Followers fetch them manually when they load their feed (saving massive write capacity).

MERGING: The final feed is a highly efficient combination of PUSHed and PULLed content.

💡 The Scalability Challenge & Hybrid Solution

Strategy

When It's Used

Read/Write Latency

The Bottleneck It Avoids

Fanout-on-Write (PUSH)

Regular Users

Fast Reads (Pre-computed)

Slow Read Latency for followers of regular users.

Fanout-on-Read (PULL)

Celebrity Users

Efficient Writes (Single entry)

Massive write storms in the feed cache.

The hybrid approach ensures that the system handles the vast volume of reads quickly (thanks to the cache for regular users) while protecting the system from write overload caused by celebrities.

🏗️ Architectural Components

The simulation architecture mirrors a typical distributed system:

Component

Package

Real-World Role

Function in this Project

SocialFeedApp

com.socialfeed

Main Application Entry Point

Orchestrates the entire simulation: initializes components, sets up follow relationships, and executes posts/feed retrievals.

FanoutWorker

com.socialfeed.pipeline

Asynchronous Microservice

Runs in a separate thread. Consumes Post IDs from the queue and executes the PUSH (for regular users) or SKIP (for celebrities) decision.

MessageQueue

com.socialfeed.pipeline

Kafka / RabbitMQ

Decouples the fast user post action from the slow fanout process, improving write latency.

FeedCache

com.socialfeed.storage

Redis Sorted Set

The destination for PUSH operations. Stores personalized feeds (Post ID + Timestamp) for rapid retrieval.

PostDatabase

com.socialfeed.storage

Sharded SQL/NoSQL

The source of truth for all post content. Used during the PULL path to fetch celebrity posts.

FollowerGraph

com.socialfeed.storage

Graph Database (Neo4j)

Stores follow relationships and determines Celebrity status based on the CELEBRITY_THRESHOLD (currently 3 followers).

⚙️ How to Run the Simulation

This project is a standalone Java application designed to be run from an IDE or command line.

Prerequisites: Ensure you have Java Development Kit (JDK) installed.

Setup: Create the necessary directory structure matching the packages (e.g., src/main/java/com/socialfeed/pipeline, src/main/java/com/socialfeed/storage, etc.) and place the .java files accordingly.

Execution: Compile and run the SocialFeedApp.java file.

Simulation Scenario Walkthrough

The SocialFeedApp.main() method runs a defined scenario demonstrating the hybrid flow for User P (ID 10), who follows two Regular Users (A=1, B=2) and one Celebrity (Z=3).

Step

User

Status

Action & Worker Result

Read/Write Strategy

Post 1

User A

Regular

Worker PUSHES Post 1 into the feeds of followers (P, Q).

Fanout-on-Write (PUSH)

Post 2

User Z

Celebrity

Worker recognizes Z's status and SKIPS the fanout. Post remains only in the PostDatabase.

Fanout-on-Read (PULL)

Post 3

User B

Regular

Worker PUSHES Post 3 into the feed of its follower (P).

Fanout-on-Write (PUSH)

Feed Load

User P

Follower

PULLS posts from the FeedCache (Post 1, Post 3) AND PULLS posts from Celebrity Z's timeline (Post 2).

Hybrid Merge

The console output will clearly show the "PUSH" and "SKIP" decisions made by the Fanout Worker and the final merged feed contents.
