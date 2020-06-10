# Stop Hiding

A Fabric version of [FindMe](https://www.curseforge.com/minecraft/mc-mods/findme), which allows you to find items in nearby inventories by hovering over them and pressing the *T* key (configurable). 

![](https://i.imgur.com/w4dXIOk.png)

Note that this mod is required on the client and server due to the way inventories work.

### Configuration

To change the key used to search nearby inventories, look in your control settings:
![](https://i.imgur.com/4rD0eoY.png)

More configuration options can be found at `config/stophiding.json5`:

- `searchRange` - Range to search for inventories within. Only valid on the server. Note that larger ranges may result in server lag.
- `displayResults`- Whether a message should be displayed above the hotbar on the client with search results.
- `highlightTime` - The time, in ticks, that a highlighted block takes to disappear.


### License

This mod is licensed under CC0 1.0.
