# INTRODUCE
<div align="center">
  <img src="images/coi_logo2.png" alt="Your Logo">
</div>

`Before forking this repo, I hope you can read Chinese so that you can understand the code notes. `  

This is a new branch for RTS Game.  

The objective of a player is to destroy all other players' bases. There can be 1-5 players in a team, and each player needs to build buildings and use AI to gather resources. The resources are then used to create weapons and armor for the AI fighters, which are sent to attack the players

# FEATURES
1. make AI run. (complete)
2. make game cycle run. (doing)

# AI INTRODUCE

There are three AIs in this game:  
1. Miner
2. Farmer
3. Soldier

All of this AI is depend on `CitizensAPI`.
All dependents are in `libs` folder.

The first AI is called "Miner" and it is a basic AI. Its main function is to locate minerals in its surroundings and extract them. When it gets hungry, it will go to the farmer room and pick food to eat. However, if the farmer room is out of food, the Miner will go back to its home and wait. This can be explained as the Miner being on strike, which is a humorous addition to the game. 

The second AI is called "Farmer" and it is similar to the original role in Minecraft. Its main task is to prepare the farmland using a hoe and sow wheat seeds. It also uses bone meal to speed up the growth of the seeds. Once the wheat ripens, the Farmer picks it and makes it into bread. Whenever he is hungry, he will eat the bread from his backpack. If he has more than 5 pieces of bread in his backpack, he will put them in the chest inside the Farmer's room so that the Miner or other AIs can find them and eat them. The Farmer works diligently, hence he is referred to as the "Proletarian Working AI".

The third and final AI is the "Soldier". Their main role is to protect other AI and fight against enemies. Players have the option to command the Soldier to follow them in battle against other players. Additionally, Soldiers are capable of destroying buildings, breaking blocks, as well as killing other players and entities.

All of these AIs create a simple resource cycle - the farmer produces food, the miner gathers minerals and also consumes food, and the fighter utilizes minerals to craft armor and weapons for attacking. With the use of this API, Minecraft can be transformed into a basic RTS game. If you are a programmer, you can incorporate all sorts of AI into your game.   

Apart from the AI, I discovered that each AI requires a home for respawn purposes. Therefore, I have implemented an automatic building feature where players can select a location to construct a structure automatically. This section of the code is similar to that of WorldEdit implementation, but it also contains some unique differences.

Hope you guys enjoy it.

# VIDEO INTRODUCE

You can have a video introduce for AI in here  

https://youtu.be/Zz-B8ijCpZM

More introductory videos are being recorded.
