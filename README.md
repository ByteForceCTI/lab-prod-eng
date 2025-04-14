This Spring project is a social network application. The main components are Users, Posts, Comments, Likes and Friendships

# User Functionalities:

 • Registration: Create new users with secure password hashing.

 • Authentication: Support login with password verification and JWT token generation.

 • Profile Retrieval: Obtain user profiles by username or ID.

 • Profile Updates: Modify username, password, profile picture, and bio.

 • Administration: Delete users and list all registered accounts.

 # Post Functionalities:

  • Post Creation: Enable users to create posts with text content, media URLs, and specific visibility settings (PUBLIC or FRIENDS_ONLY).

 • Post Update Management: Allow authenticated users to update their own posts, modifying content and media while recording update timestamps.

 • Post Deletion with Cascade Effects: Support the deletion of posts while automatically removing associated comments and likes.

 • Visibility Control Based on Friendships: Enforce visibility rules by evaluating friendship statuses to determine post accessibility.

# Comment Functionalities:

 • Comment Creation: Users can post new top-level comments on posts.

 • Nested Comments: Provides the ability to reply to existing comments by linking them as nested comments.

 • Comment Management: Supports editing and deleting individual comments to maintain content accuracy.

 • Comment Counting: Allows for calculating the total number of comments associated with a specific post.

 • Cascade Deletion: Deletes all comments for a post while also removing their associated likes.


# Friendship Functionalities:

 • Send Friend Request: Initiate a friendship by sending a request (with userId1 and userId2), automatically setting the status to PENDING.

 • Manage Requests: Accept a friend request (updating status to ACCEPTED), or reject it by deleting the request.

 • Block Friend: Update the friendship status to BLOCKED to prevent further interactions.

 • Retrieve Friendships: Fetch all friendship records associated with a specific user.

# Like Functionalities:

 • Post & Comment Likes: Users can add likes to posts and comments, with validation to prevent duplicate likes.

 • Counting: Calculate the total number of likes associated with a specific post or comment.

 • Deletion: Remove individual likes by ID, and support deletion of all likes for a post or comment when necessary.
