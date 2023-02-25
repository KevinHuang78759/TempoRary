Acceleration Strucutre:

We break the screen up into cells, and hash the cell coordinates into into integers so that we can use a map that
maps Integers to Lists of objects. When checking for collisions, we check only the cell in which the object's
center lies in, then we determine which cells the object might overlap with and check for collisions in the 
overlapping cells as well. Note that since we check for overlaps, we do not have to add the object to the
overlapping cells. This is because we check the nearest cells first, so if we find a collision then we get instantly 
pushed away in the other direction. We keep the cells sufficiently large such that there are no cases where
two objects collide when they are more than 1 cell apart. There is also the remnants of a splitting-cell tree
structure commented out, where rather than defining cells based purely on the static coordinate grid, we 
set a capacity on the number of objects that can be in each cell, then dynamically split the cell into 4 child-cells,
each representing one quadrant. However, with a large number of objects the tree became very deep and it was ultimately 
deemed inefficient.

Gameplay Changes:

Added a points system:
If a bullet fired specifically from a ship were to fully destroy a shell, the player is awarded with 1 point. Chain reactions
will not award points to the player.
Upon death of the player, the points are reset. The number of points are displayed under the shell count 
on the top left of the screen. We modified bullet to include whether or not the bullet was generated from a ship, and added
instance variables and appropriate modifiers to Ship to accomodate this. We changed the GameMode class as well so that we can display 
the points.

Added high score system: The highest score achieved in one play run is saved. If the high score is exceeded during the run,
the high score marker will update dynamically. The high score is also displayed on the top left of the screen, under the point
counter. The high score always starts at 0 on the first run through. If a new high score is achieved, another text will appear
on the game over screen indicating this, as well as the new high score. The high score is only set back to 0 if the game is closed out.
We modified GameMode to accomodate drawing the high score, and added the appropriate modifiers, accessors and instance variables to GameplayController.

Added an invicibility as a reward: The player becomes invincible (but still susceptible to collision knockback) for 600
frames (which translates to about 10 seconds if we are running at 600 frames per second) every time the player scores 50 points.
Text will appear on the top left of the screen under the high score indicator when the player is invincible.
We changed the ship to shell handler in CollisionController to accomodate this, as well as adding appropriate instance variables
and accessor methods to the Ship class.



Files modified: Ship.java, Bullet.java, CollisionController.java, GameplayController.java, GameMode.java

No extra assets were used.