# COI REALM INTRODUCE

`Before you fork this repo,I hope you can read Chinese.
Then you can read the code notes.  `  

This is a new branch for RTS Game.  

Player's object is destroy all others' player base.
1 - 5 players in 1 team,player need build building and use AI to help them get resources,
then use the resources make weapons and armors for AI fighter,
and let them attack the other's player.

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

The first is `miner`.It's a basic AI.
He will find out that the mineral around himself and excavate it.
When he is hungry,he will go to the farmer room and pick the food to eat.
I know u will ask `'what if farmer room don't have any food,what will the miner do?'`
Answer is he will go back to he's home,and wait.
We can explain that he is on strike.
I think it's very funny.   

The second is `farmer`,this AI just like origin role in Minecraft.
He will use hoe and make dirty to farmland,then he will put the wheat seed on it.
Also he will use bone meal to make the seeds grow up.
When the wheat ripened,he will pick it and make it to a bread,
and when he is hungry,he will eat the bread from his backpack.
If his backpack has more than 5 bread,he will put them into the farmer room's chest.
Then miner or the others could find them and pick to eat.
Yeah,he worked very hard,so i called him Proletarian working AI.  

The final one is `Soldier`.
This AI will fight with enemies and protect the other AI.
Players can let the fighter follow them to attack another player.
The Soldier also can destory the building,break block,kill player and entities.  

all of these AIs makes a simple resource cycle,
the farmer make food,miner get minerals and eat foods,
fighter use minerals to make armor and weapons to attack.
It can be a very simple RTS game in MC,if u are programmer,
u can use this API to create every kind of AI into u game.   

Expect the AI,I realized that all the AIs need a home to respawn,
so I implement an automatic building???
player can choose a place to build structure by auto.
This part of code is same as WorldEdit Implement but also have some different.  

Hope u guys enjoy it.

# VIDEO INTRODUCE

You can have a video introduce for AI in here  

https://youtu.be/Zz-B8ijCpZM

More introductory videos are being recorded.
