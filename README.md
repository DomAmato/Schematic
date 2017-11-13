# Schematic
## Minecraft Schematics Mod
A self contained mod for handling schematics in game. Schematics in the game directorty schematic folder are auto loaded into the creative menu and can also be created using an empty schematic and selecting two points to make a new one. Loaded schematics show a material cost and size along with the blocks used. Large schematics are threaded and chunked to keep lag down to a minimum.

### Blocks and Items

1. Schematic
   The Schematic item is the main component of the mod, you can craft a blank one and then convert it to one from a saved schematic using the architect's desk.  If you want to create your own right clicking with a blank schematic in hand will set the corners of a new schematic and left clicking a block after both corners are set will prompt if you want to create a new schematic. 

![alt text](https://github.com/DomAmato/Schematic/blob/master/images/schematic.png "Schematic")

2. Architect's Desk
   The Architect's Desk allows you to convert a blank Schematic into one that is saved in your minecraft data folder. Generating a schematic this way costs gold proportional to the size with each 500 blocks increasing the price 1 ingot.

![alt text](https://github.com/DomAmato/Schematic/blob/master/images/desk.png "Architect's Desk")

### Using the Items
#### The Desk
![alt text](https://github.com/DomAmato/Schematic/blob/master/images/gui/desk_empty.png "Empty Desk")

   When you first click the desk it is empty, you can place a schematic in the lower left slot. Notice only your hotbar is accessible so make sure you have space and the right items.

![alt text](https://github.com/DomAmato/Schematic/blob/master/images/gui/desk_1.png "Missing the Gold")

   You can preview the item without paying for the gold and the top 5 most common materials are listed in order.
   
![alt text](https://github.com/DomAmato/Schematic/blob/master/images/gui/desk_filled.png "Paying")
   
And viola you have a new schematic based on a saved file. Now its easy to bring it schematics you downloaded online

#### The Schematic
##### Preview and Build
![alt text](https://github.com/DomAmato/Schematic/blob/master/images/buildin/place.png "Place the Schematic")

   You can place the schematic on a wall or on a ceiling facing any direction and it will build from that position. Note that it will only build upwards since there are certain issues with rotating schematics that might be solved later but for now this is the method.
   
![alt text](https://github.com/DomAmato/Schematic/blob/master/images/buildin/preview.png "Preview the Schematic")

   Left click it once and it will preview the schematic. If you shift left click it during this phase it will rotate 90 degrees. Now you can see your build before to make sure its in the right spot. Don't worry breaking the sign will return the schematic to you.
   
![alt text](https://github.com/DomAmato/Schematic/blob/master/images/buildin/ask.png "Do you want to build")

   Left click it again and it will ask if you want to build the schematic. If you are in creative it will build it without any extra processing, but in survival it will cost you. Whatever building materials are required to build the structure are needed or it will give an error. You don't need 100% exact materials as long as the materials is the right type, like stained terracotta can be any color, same with wool etc...
   
![alt text](https://github.com/DomAmato/Schematic/blob/master/images/buildin/error.png "Error!")

![alt text](https://github.com/DomAmato/Schematic/blob/master/images/buildin/built.png "Yay")

##### Selecting and Creating Schematics
![alt text](https://github.com/DomAmato/Schematic/blob/master/images/select/corner1.png "Select Corner 1")

   With an empty schematic if you want to create a brand new schematic, you totally can. Just left click a block to set the first corner.
   
![alt text](https://github.com/DomAmato/Schematic/blob/master/images/select/corner2.png "Select Corner 2")

   Then select the other corner, note you cannot select air so you might have to build a tower in order to select the area you need.
   
![alt text](https://github.com/DomAmato/Schematic/blob/master/images/select/ask.png "Do you want to build")

   Right click on a block and you will be asked if you want to save the schematic that encompasses the area that has been selected. If you dont like the area you selected just right click in the air and it will blank out the selected areas.
   
![alt text](https://github.com/DomAmato/Schematic/blob/master/images/select/success.png "Success!")

   And success you now have a schematic that is the area you highlighted. It is named after you and the time it was was created, and eventually you will be able to rename it and save it client side. Currently it saves server side but it is your file after all you should have it on your computer!
