@file:Suppress("NAME_SHADOWING")

package main.ui
import com.badlogic.gdx.graphics.Color.*
import com.badlogic.gdx.graphics.GL20.GL_TEXTURE_2D
import com.badlogic.gdx.graphics.Texture.TextureFilter.*
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER
import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Buttons.LEFT
import com.badlogic.gdx.Input.Buttons.MIDDLE
import com.badlogic.gdx.Input.Buttons.RIGHT
import com.badlogic.gdx.Input.Keys.*
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Color.*
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.DEFAULT_CHARS
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import main.*
import main.deserializer.channel.ActorChannel.Companion.actorHasWeapons
import main.deserializer.channel.ActorChannel.Companion.actors
import main.deserializer.channel.ActorChannel.Companion.airDropLocation
import main.deserializer.channel.ActorChannel.Companion.corpseLocation
import main.deserializer.channel.ActorChannel.Companion.droppedItemLocation
import main.deserializer.channel.ActorChannel.Companion.visualActors
import main.deserializer.channel.ActorChannel.Companion.weapons
import main.struct.Actor
import main.struct.Archetype
import main.struct.Archetype.*
import main.struct.NetworkGUID
import main.struct.cmd.ActorCMD.actorHealth
import main.struct.cmd.ActorCMD.actorWithPlayerState
import main.struct.cmd.ActorCMD.playerStateToActor
import main.struct.cmd.GameStateCMD.ElapsedWarningDuration
import main.struct.cmd.GameStateCMD.IsTeamMatch
import main.struct.cmd.GameStateCMD.NumAlivePlayers
import main.struct.cmd.GameStateCMD.NumAliveTeams
import main.struct.cmd.GameStateCMD.PoisonGasWarningPosition
import main.struct.cmd.GameStateCMD.PoisonGasWarningRadius
import main.struct.cmd.GameStateCMD.RedZonePosition
import main.struct.cmd.GameStateCMD.RedZoneRadius
import main.struct.cmd.GameStateCMD.SafetyZonePosition
import main.struct.cmd.GameStateCMD.SafetyZoneRadius
import main.struct.cmd.GameStateCMD.TotalWarningDuration
import main.struct.cmd.PlayerStateCMD.attacks
import main.struct.cmd.PlayerStateCMD.playerNames
import main.struct.cmd.PlayerStateCMD.playerNumKills
import main.struct.cmd.PlayerStateCMD.selfID
import main.struct.cmd.PlayerStateCMD.selfStateID
import main.struct.cmd.PlayerStateCMD.teamNumbers
import main.struct.cmd.TeamCMD.team
import main.struct.cmd.selfAttachTo
import main.struct.cmd.selfCoords
import main.struct.cmd.selfDirection
import main.util.tuple4
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow

typealias renderInfo = tuple4<Actor, Float, Float, Float>

class GLMap : InputAdapter(), ApplicationListener, GameListener {
    companion object {
        operator fun Vector3.component1(): Float = x
        operator fun Vector3.component2(): Float = y
        operator fun Vector3.component3(): Float = z
        operator fun Vector2.component1(): Float = x
        operator fun Vector2.component2(): Float = y
    }

    init {
        register(this)
    }


    override fun onGameStart() {
        selfCoords.setZero()
        selfAttachTo = null
    }

    override fun onGameOver() {
        camera.zoom = 2 / 4f

        aimStartTime.clear()
        attackLineStartTime.clear()
        pinLocation.setZero()
    }

    fun show() {
        val config = Lwjgl3ApplicationConfiguration()
        config.setTitle("")
        config.useOpenGL3(false, 2, 1)
        config.setWindowedMode(800, 800)
        config.setResizable(true)
        config.setBackBufferConfig(4, 4, 4, 4, 16, 4, 8)
        Lwjgl3Application(this, config)

    }

	private var playersize = 5f
	
    private lateinit var spriteBatch: SpriteBatch
    private lateinit var shapeRenderer: ShapeRenderer
	lateinit var mapErangel: Texture
	lateinit var mapMiramar: Texture
	private lateinit var DaMap: Texture
    private lateinit var iconImages: Icons
    private lateinit var corpseboximage: Texture
    private lateinit var airdropimage: Texture
    private lateinit var bgcompass: Texture
    private lateinit var menu: Texture
    private lateinit var largeFont: BitmapFont
    private lateinit var littleFont: BitmapFont
    private lateinit var nameFont: BitmapFont
    private lateinit var itemFont: BitmapFont
    private lateinit var hporange: BitmapFont
    private lateinit var hpred: BitmapFont
    private lateinit var hpgreen: BitmapFont
    private lateinit var menuFont: BitmapFont
    private lateinit var menuFontOn: BitmapFont
    private lateinit var menuFontOFF: BitmapFont
    private lateinit var fontCamera: OrthographicCamera
    private lateinit var itemCamera: OrthographicCamera
    private lateinit var camera: OrthographicCamera
    private lateinit var alarmSound: Sound
    private lateinit var hubpanel: Texture
    private lateinit var hubpanelblank: Texture
    private lateinit var vehicle: Texture
    private lateinit var plane: Texture
    private lateinit var boat: Texture
    private lateinit var bike: Texture
    private lateinit var bike3x: Texture
    private lateinit var buggy: Texture
    private lateinit var van: Texture
    private lateinit var pickup: Texture
    private lateinit var arrow: Texture
    private lateinit var arrowsight: Texture
    private lateinit var jetski: Texture
    private lateinit var player: Texture
	private lateinit var teamplayer: Texture
    private lateinit var playersight: Texture
    private lateinit var parachute: Texture
    private lateinit var grenade: Texture
    private lateinit var hubFont: BitmapFont
    private lateinit var hubFontShadow: BitmapFont
    private lateinit var espFont: BitmapFont
    private lateinit var espFontShadow: BitmapFont
    private lateinit var compaseFont: BitmapFont
    private lateinit var compaseFontShadow: BitmapFont
    private lateinit var littleFontShadow: BitmapFont

    private val layout = GlyphLayout()
    private var windowWidth = initialWindowWidth
    private var windowHeight = initialWindowWidth

    private val aimStartTime = HashMap<NetworkGUID, Long>()
    private val attackLineStartTime = LinkedList<Triple<NetworkGUID, NetworkGUID, Long>>()
    private val pinLocation = Vector2()
    // Menu Settings
    //////////////////////////////
	private var filterWeapon = -1
    private var filterAttach = 1
    private var filterLvl2 = -1
    private var filterScope = -1
	private var filterHeals = 1
	private var filterAmmo = 1
	private var filterThrow = 1
    private var drawcompass = -1
    private var drawmenu = 1
    private var toggleView = -1
    private var drawgrid = -1
    private var nameToggles = 4
    private var VehicleInfoToggles = 1
    private var ZoomToggles = 1
    ///////////////////////////
    private var scopesToFilter = arrayListOf("")
    private var weaponsToFilter = arrayListOf("")
    private var attachToFilter = arrayListOf("")
    private var level2Filter = arrayListOf("")
    private var healsToFilter = arrayListOf("")
    private var ammoToFilter = arrayListOf("")
    private var throwToFilter = arrayListOf("")
	private var uselessToFilter = arrayListOf("")
    private var dragging = false
    private var prevScreenX = -1f
    private var prevScreenY = -1f
    private var screenOffsetX = 0f
    private var screenOffsetY = 0f



    // Origin Offset
    private var originYlazy = 1.15000014f
    private var originXlazy = -0.45000014f

    private fun windowToMap(x: Float, y: Float) =
            Vector2(selfCoords.x + (x - windowWidth / 2.0f) * camera.zoom * windowToMapUnit + screenOffsetX,
                    selfCoords.y + (y - windowHeight / 2.0f) * camera.zoom * windowToMapUnit + screenOffsetY)

    private fun mapToWindow(x: Float, y: Float) =
            Vector2((x - selfCoords.x - screenOffsetX) / (camera.zoom * windowToMapUnit) + windowWidth / 2.0f,
                    (y - selfCoords.y - screenOffsetY) / (camera.zoom * windowToMapUnit) + windowHeight / 2.0f)

    fun Vector2.mapToWindow() = mapToWindow(x, y)
    fun Vector2.windowToMap() = windowToMap(x, y)


    override fun scrolled(amount: Int): Boolean {

		if (camera.zoom > 0.04f && camera.zoom < 1.09f) {
            camera.zoom *= 1.05f.pow(amount)
        } else {
            if (camera.zoom < 0.04f) {
                camera.zoom = 0.041f
            //    println("Max Zoom")
            }
            if (camera.zoom > 1.09f) {
                camera.zoom = 1.089f
            //    println("Min Zoom")
            }
        }

        return true
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        when (button) {
            RIGHT -> {
                pinLocation.set(pinLocation.set(screenX.toFloat(), screenY.toFloat()).windowToMap())
                camera.update()
            //    println(pinLocation)
                return true
            }
            LEFT -> {
                dragging = true
                prevScreenX = screenX.toFloat()
                prevScreenY = screenY.toFloat()
                return true
            }
            MIDDLE -> {
                screenOffsetX = 0f
                screenOffsetY = 0f
            }
        }
        return false
    }

    override fun keyDown(keycode: Int): Boolean {

        when (keycode) {


        // Change Player Info
            F1 -> {
                if (nameToggles < 6) {nameToggles += 1}
                if (nameToggles == 6) {nameToggles = 1}
            }

            F5 -> {
                if (VehicleInfoToggles <= 4) {VehicleInfoToggles += 1}
                if (VehicleInfoToggles == 4) {VehicleInfoToggles = 1}
            }
        // Zoom (Loot, Combat, Scout)
            NUMPAD_8 -> {
                if (ZoomToggles <= 4) { ZoomToggles += 1}
                if (ZoomToggles == 4) { ZoomToggles = 1}
                // then
                if (ZoomToggles == 1) {camera.zoom = 1 / 4f}
                // or
                if (ZoomToggles == 2) {camera.zoom = 1 / 9f}
                // or
                if (ZoomToggles == 3) {camera.zoom = 1 / 24f}
            }
        // Other Filter Keybinds
            F2 -> drawcompass = drawcompass * -1
            F3 -> drawgrid = drawgrid * -1

        // Toggle View Line
            F4 -> toggleView = toggleView * -1

        // Toggle Vehicles
        //  F5 -> toggleVehicles = toggleVehicles * -1
        //  F6 -> toggleVNames = toggleVNames * -1

        // Toggle Menu
            F12 -> drawmenu = drawmenu * -1

        // Icon Filter Keybinds
            NUMPAD_0 -> filterWeapon = filterWeapon * -1
			NUMPAD_1 -> filterAttach = filterAttach * -1
			NUMPAD_2 -> filterScope = filterScope * -1
			NUMPAD_3 -> filterAmmo = filterAmmo * -1
			NUMPAD_4 -> filterLvl2 = filterLvl2 * -1
			NUMPAD_5 -> filterHeals = filterHeals * -1
			NUMPAD_6 -> filterThrow = filterThrow * -1

        // Zoom In/Out || Overrides Max/Min Zoom
            MINUS -> camera.zoom = camera.zoom + 0.00525f
            PLUS -> camera.zoom = camera.zoom - 0.00525f
        }
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        if (!dragging) return false
        with(camera) {
            screenOffsetX += (prevScreenX - screenX.toFloat()) * camera.zoom * 500
            screenOffsetY += (prevScreenY - screenY.toFloat()) * camera.zoom * 500
            prevScreenX = screenX.toFloat()
            prevScreenY = screenY.toFloat()
        }
        return true
    }


    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (button == LEFT) {
            dragging = false
            return true
        }
        return false
    }

    override fun create() {
        spriteBatch = SpriteBatch()
        shapeRenderer = ShapeRenderer()
        Gdx.input.inputProcessor = this
        camera = OrthographicCamera(windowWidth, windowHeight)
        with(camera) {
            setToOrtho(true, windowWidth * windowToMapUnit, windowHeight * windowToMapUnit)
            zoom = 1 / 4f
            update()
            position.set(mapWidth / 2, mapWidth / 2, 0f)
            update()
        }

        itemCamera = OrthographicCamera(initialWindowWidth, initialWindowWidth)
        fontCamera = OrthographicCamera(initialWindowWidth, initialWindowWidth)
        alarmSound = Gdx.audio.newSound(Gdx.files.internal("sounds/Alarm.wav"))
        hubpanel = Texture(Gdx.files.internal("images/hub_panel.png"))
        bgcompass = Texture(Gdx.files.internal("images/bg_compass.png"))
        menu = Texture(Gdx.files.internal("images/menu.png"))
        hubpanelblank = Texture(Gdx.files.internal("images/hub_panel_blank_long.png"))
        corpseboximage = Texture(Gdx.files.internal("icons/box.png"))
        airdropimage = Texture(Gdx.files.internal("icons/airdrop.png"))
        vehicle = Texture(Gdx.files.internal("images/vehicle.png"))
        arrow = Texture(Gdx.files.internal("images/arrow.png"))
        plane = Texture(Gdx.files.internal("images/plane.png"))
        player = Texture(Gdx.files.internal("images/player.png"))
		teamplayer = Texture(Gdx.files.internal("images/team.png"))
        playersight = Texture(Gdx.files.internal("images/green_view_line.png"))
        arrowsight = Texture(Gdx.files.internal("images/red_view_line.png"))
        parachute = Texture(Gdx.files.internal("images/parachute.png"))
        boat = Texture(Gdx.files.internal("images/boat.png"))
        bike = Texture(Gdx.files.internal("images/bike.png"))
        jetski = Texture(Gdx.files.internal("images/jetski.png"))
        bike3x = Texture(Gdx.files.internal("images/bike3x.png"))
        pickup = Texture(Gdx.files.internal("images/pickup.png"))
        van = Texture(Gdx.files.internal("images/van.png"))
        buggy = Texture(Gdx.files.internal("images/buggy.png"))
        grenade = Texture(Gdx.files.internal("images/grenade.png"))
        iconImages = Icons(Texture(Gdx.files.internal("images/item-sprites.png")), 64)
        var cur = 0

		glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, floatArrayOf(bgColor.r, bgColor.g, bgColor.b, bgColor.a))
		mapErangel = Texture(Gdx.files.internal("maps/Erangel.png"), null, true).apply {
			setFilter(MipMap, Linear)
			Gdx.gl.glTexParameterf(glTarget, GL20.GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER.toFloat())
			Gdx.gl.glTexParameterf(glTarget, GL20.GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER.toFloat())
		}
		mapMiramar = Texture(Gdx.files.internal("maps/Miramar.png"), null, true).apply {
			setFilter(MipMap, Linear)
			Gdx.gl.glTexParameterf(glTarget, GL20.GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER.toFloat())
			Gdx.gl.glTexParameterf(glTarget, GL20.GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER.toFloat())
		}
		
        val generatorHub = FreeTypeFontGenerator(Gdx.files.internal("font/AGENCYFB.TTF"))
        val paramHub = FreeTypeFontParameter()
        paramHub.characters = DEFAULT_CHARS
        paramHub.size = 30
        paramHub.color = WHITE
        hubFont = generatorHub.generateFont(paramHub)
        paramHub.color = Color(1f, 1f, 1f, 0.4f)
        hubFontShadow = generatorHub.generateFont(paramHub)
        paramHub.size = 16
        paramHub.color = WHITE
        espFont = generatorHub.generateFont(paramHub)
        paramHub.color = Color(1f, 1f, 1f, 0.2f)
        espFontShadow = generatorHub.generateFont(paramHub)
        val generatorNumber = FreeTypeFontGenerator(Gdx.files.internal("font/NUMBER.TTF"))
        val paramNumber = FreeTypeFontParameter()
        paramNumber.characters = DEFAULT_CHARS
        paramNumber.size = 24
        paramNumber.color = WHITE
        largeFont = generatorNumber.generateFont(paramNumber)
        val generator = FreeTypeFontGenerator(Gdx.files.internal("font/GOTHICB.TTF"))
        val param = FreeTypeFontParameter()
        param.characters = DEFAULT_CHARS
        param.size = 38
        param.color = WHITE
        largeFont = generator.generateFont(param)
        param.size = 15
        param.color = WHITE
        littleFont = generator.generateFont(param)
        param.color = BLACK
        param.size = 10
        nameFont = generator.generateFont(param)
        param.color = WHITE
        param.size = 6
        itemFont = generator.generateFont(param)
        val compaseColor = Color(0f, 0.95f, 1f, 1f)  //Turquoise1
        param.color = compaseColor
        param.size = 10
        compaseFont = generator.generateFont(param)
        param.color = Color(0f, 0f, 0f, 0.5f)
        compaseFontShadow = generator.generateFont(param)
        param.characters = DEFAULT_CHARS
        param.size = 20
        param.color = WHITE
        littleFont = generator.generateFont(param)
        param.color = Color(0f, 0f, 0f, 0.5f)
        littleFontShadow = generator.generateFont(param)
        param.color = WHITE
        param.size = 12
        menuFont = generator.generateFont(param)
        param.color = GREEN
        param.size = 12
        menuFontOn = generator.generateFont(param)
        param.color = RED
        param.size = 12
        menuFontOFF = generator.generateFont(param)
        param.color = ORANGE
        param.size = 10
        hporange = generator.generateFont(param)
        param.color = GREEN
        param.size = 10
        hpgreen = generator.generateFont(param)
        param.color = RED
        param.size = 10
        hpred = generator.generateFont(param)


        generatorHub.dispose()
        generatorNumber.dispose()
        generator.dispose()
    }

    override fun render() {
		Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, bgColor.a)
        Gdx.gl.glClearColor(0.417f, 0.417f, 0.417f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        if (gameStarted)
            DaMap = if (isErangel) mapErangel else mapMiramar
        else return
        val currentTime = System.currentTimeMillis()
        // Maybe not needed, could be draw error

        selfAttachTo?.apply {
            selfCoords.set(location.x, location.y)
            selfDirection = rotation.y
        }

        val (selfX, selfY) = selfCoords

        //move camera
        camera.position.set(selfX + screenOffsetX, selfY + screenOffsetY, 0f)
        camera.update()
        
        paint(camera.combined) {
            draw(DaMap, 0f, 0f, mapWidth, mapWidth, 0, 0, DaMap.width, DaMap.height, false, true)
        }

        shapeRenderer.projectionMatrix = camera.combined
        Gdx.gl.glEnable(GL20.GL_BLEND)

        drawCircles()

        val typeLocation = EnumMap<Archetype, MutableList<renderInfo>>(Archetype::class.java)
        for ((_, actor) in visualActors)
            typeLocation.compute(actor.Type) { _, v ->
                val list = v ?: ArrayList()
                val (centerX, centerY) = actor.location
                val direction = actor.rotation.y
                list.add(tuple4(actor, centerX, centerY, direction))
                list
            }

        val playerStateGUID = actorWithPlayerState[selfID] ?: 0
        val numKills = playerNumKills[playerStateGUID] ?: 0
        val zero = numKills.toString()
        paint(fontCamera.combined) {

            // NUMBER PANEL
            val numText = "$NumAlivePlayers"
            layout.setText(hubFont, numText)
            spriteBatch.draw(hubpanel, windowWidth - 130f, windowHeight - 60f)
            hubFontShadow.draw(spriteBatch, "ALIVE", windowWidth - 85f, windowHeight - 29f)
            hubFont.draw(spriteBatch, "$NumAlivePlayers", windowWidth - 110f - layout.width / 2, windowHeight - 29f)
            val teamText = "$NumAliveTeams"


            if (IsTeamMatch) {
                layout.setText(hubFont, teamText)
                spriteBatch.draw(hubpanel, windowWidth - 260f, windowHeight - 60f)
                hubFontShadow.draw(spriteBatch, "TEAM", windowWidth - 215f, windowHeight - 29f)
                hubFont.draw(spriteBatch, "$NumAliveTeams", windowWidth - 240f - layout.width / 2, windowHeight - 29f)
            }

            if (IsTeamMatch) {

                layout.setText(hubFont, zero)
                spriteBatch.draw(hubpanel, windowWidth - 390f, windowHeight - 60f)
                hubFontShadow.draw(spriteBatch, "KILLS", windowWidth - 345f, windowHeight - 29f)
                hubFont.draw(spriteBatch, "$zero", windowWidth - 370f - layout.width / 2, windowHeight - 29f)
            } else {
                spriteBatch.draw(hubpanel, windowWidth - 390f + 130f, windowHeight - 60f)
                hubFontShadow.draw(spriteBatch, "KILLS", windowWidth - 345f + 128f, windowHeight - 29f)
                hubFont.draw(spriteBatch, "$zero", windowWidth - 370f + 128f - layout.width / 2, windowHeight - 29f)

            }


            // ITEM ESP FILTER PANEL
            spriteBatch.draw(hubpanelblank, 30f, windowHeight - 60f)

            // This is what you were trying to do
            if (filterWeapon != 1)
                espFont.draw(spriteBatch, "WEAPON", 40f, windowHeight - 25f)
            else
                espFontShadow.draw(spriteBatch, "WEAPON", 40f, windowHeight - 25f)

            if (filterAttach != 1)
                espFont.draw(spriteBatch, "ATTACH", 40f, windowHeight - 42f)
            else
                espFontShadow.draw(spriteBatch, "ATTACH", 40f, windowHeight - 42f)

            if (filterScope != 1)
                espFont.draw(spriteBatch, "SCOPE", 100f, windowHeight - 25f)
            else
                espFontShadow.draw(spriteBatch, "SCOPE", 100f, windowHeight - 25f)
				
			if (filterAmmo != 1)
                espFont.draw(spriteBatch, "AMMO", 100f, windowHeight - 42f)
            else
                espFontShadow.draw(spriteBatch, "AMMO", 100f, windowHeight - 42f)
				
			if (filterLvl2 != 1)
                espFont.draw(spriteBatch, "EQUIP", 150f, windowHeight - 25f)
            else
                espFontShadow.draw(spriteBatch, "EQUIP", 150f, windowHeight - 25f)
				
            if (filterHeals != 1)
                espFont.draw(spriteBatch, "HEALS", 150f, windowHeight - 42f)
            else
                espFontShadow.draw(spriteBatch, "HEALS", 150f, windowHeight - 42f)

            if (filterThrow != 1)
                espFont.draw(spriteBatch, "BOMBS", 200f, windowHeight - 25f)
            else
                espFontShadow.draw(spriteBatch, "BOMBS", 200f, windowHeight - 25f)
				
            if (drawcompass == 1)
                espFont.draw(spriteBatch, "COMPASS", 200f, windowHeight - 42f)
            else
                espFontShadow.draw(spriteBatch, "COMPASS", 200f, windowHeight - 42f)

            if (drawmenu == 1)
                espFont.draw(spriteBatch, "[INS] Menu ON", 270f, windowHeight - 25f)
            else
                espFontShadow.draw(spriteBatch, "[INS] Menu OFF", 270f, windowHeight - 25f)


            val pinDistance = (pinLocation.cpy().sub(selfX, selfY).len() / 100).toInt()
            val (x, y) = pinLocation.mapToWindow()

            safeZoneHint()
            drawPlayerNames(typeLocation[Player], selfX, selfY)

            val camnum = camera.zoom

            if (drawmenu == 1) {
                spriteBatch.draw(menu, 20f, windowHeight / 2 - 200f)

                // Filters
                if (filterWeapon != 1)
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + 103f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + 103f)

                if (filterAttach != 1)
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + 85f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + 85f)

                if (filterScope != 1)
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + 67f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + 67f)

                if (filterAmmo != 1)
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + 49f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + 49f)

                if (filterLvl2 != 1)
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + 31f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + 31f)

                if (filterHeals != 1)
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + 13f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + 13f)

                if (filterThrow != 1)
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + -5f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + -5f)

                val camvalue = camera.zoom
                when {
                    camvalue <= 0.0100f -> menuFontOFF.draw(spriteBatch, "Max Zoom", 187f, windowHeight / 2 + -27f)
                    camvalue >= 1f -> menuFontOFF.draw(spriteBatch, "Min Zoom", 187f, windowHeight / 2 + -27f)
                    camvalue == 0.2500f -> menuFont.draw(spriteBatch, "Default", 187f, windowHeight / 2 + -27f)
                    camvalue == 0.1250f -> menuFont.draw(spriteBatch, "Scouting", 187f, windowHeight / 2 + -27f)
                    camvalue >= 0.0833f -> menuFont.draw(spriteBatch, "Combat", 187f, windowHeight / 2 + -27f)
                    camvalue <= 0.0417f -> menuFont.draw(spriteBatch, "Looting", 187f, windowHeight / 2 + -27f)

                    else -> menuFont.draw(spriteBatch, ("%.4f").format(camnum), 187f, windowHeight / 2 + -27f)
                }

                // Name Toggles
                val togs = nameToggles
                if (nameToggles != 1)

                    menuFontOn.draw(spriteBatch, "Enabled: $togs", 187f, windowHeight / 2 + -89f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + -89f)


                // Compass
                if (drawcompass != 1)

                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + -107f)
                else
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + -107f)

                // Grid
                if (drawgrid == 1)

                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + -125f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + -125f)

                if (toggleView == 1)
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + -143f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + -143f)

                if (VehicleInfoToggles < 3)
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + -161f)
                if (VehicleInfoToggles == 3)
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + -161f)

                // DrawMenu == 1 already
                menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + -179f)
            }
            // DrawMenu == 0 (Disabled)


            if (drawcompass == 1) {

                spriteBatch.draw(bgcompass, windowWidth / 2 - 168f, windowHeight / 2 - 168f)

                layout.setText(compaseFont, "0")
                compaseFont.draw(spriteBatch, "0", windowWidth / 2 - layout.width / 2, windowHeight / 2 + layout.height + 150)                  // N
                layout.setText(compaseFont, "45")
                compaseFont.draw(spriteBatch, "45", windowWidth / 2 - layout.width / 2 + 104, windowHeight / 2 + layout.height / 2 + 104)          // NE
                layout.setText(compaseFont, "90")
                compaseFont.draw(spriteBatch, "90", windowWidth / 2 - layout.width / 2 + 147, windowHeight / 2 + layout.height / 2)                // E
                layout.setText(compaseFont, "135")
                compaseFont.draw(spriteBatch, "135", windowWidth / 2 - layout.width / 2 + 106, windowHeight / 2 + layout.height / 2 - 106)          // SE
                layout.setText(compaseFont, "180")
                compaseFont.draw(spriteBatch, "180", windowWidth / 2 - layout.width / 2, windowHeight / 2 + layout.height / 2 - 151)                // S
                layout.setText(compaseFont, "225")
                compaseFont.draw(spriteBatch, "225", windowWidth / 2 - layout.width / 2 - 109, windowHeight / 2 + layout.height / 2 - 109)          // SW
                layout.setText(compaseFont, "270")
                compaseFont.draw(spriteBatch, "270", windowWidth / 2 - layout.width / 2 - 153, windowHeight / 2 + layout.height / 2)                // W
                layout.setText(compaseFont, "315")
                compaseFont.draw(spriteBatch, "315", windowWidth / 2 - layout.width / 2 - 106, windowHeight / 2 + layout.height / 2 + 106)          // NW
            }
            littleFont.draw(spriteBatch, "$pinDistance", x, windowHeight - y)


        }

        if (drawgrid == 1) {
            drawGrid()

        }


        // This makes the array empty if the filter is off for performance with an inverted function since arrays are expensive
        scopesToFilter = if (filterScope != 1) {
            arrayListOf("")
        } else {
            arrayListOf("DotSight", "Aimpoint", "Holosight", "CQBSS", "ACOG")
        }


        attachToFilter = if (filterAttach != 1) {
            arrayListOf("")
        } else {
            arrayListOf("AR.Stock", "S.Loops", "CheekPad", "A.Grip", "V.Grip", "U.Ext", "AR.Ext", "S.Ext", "U.ExtQ", "AR.ExtQ", "S.ExtQ", "Choke", "AR.Comp", "FH", "U.Supp",/* "AR.Supp",*/ "S.Supp")
        }

        weaponsToFilter = if (filterWeapon != 1) {
            arrayListOf("")
        } else {
            arrayListOf("M16A4", "HK416", "Kar98k", "SCAR-L", "AK47", "SKS", "AUG", "M249", "AWM", "Groza", "M24", "MK14", "Mini14", "DP28", "UMP", "Vector", "UZI", "Pan")
        }

        healsToFilter = if (filterHeals != 1) {
            arrayListOf("")
        } else {
            arrayListOf("Bandage", "FirstAid", "MedKit", "Drink", "Pain", "Syringe")
        }

        ammoToFilter = if (filterAmmo != 1) {
            arrayListOf("")
        } else {
            arrayListOf("9mm", "45mm", "556mm", "762mm", "300mm")
        }

        throwToFilter = if (filterThrow != 1) {
            arrayListOf("")
        } else {
            arrayListOf("Grenade", "FlashBang", "SmokeBomb", "Molotov")
        }

        level2Filter = if (filterLvl2 != 1) {
            arrayListOf("")
        } else {
            arrayListOf("Bag2", "Armor2", "Helmet2")
        }

		uselessToFilter = arrayListOf("AR.Stock", "A.Grip", "U.Ext", "AR.Ext", "S.Ext", "SmokeBomb", "FlashBang", "45mm", "DotSight", "Holosight", "Aimpoint") 

        val iconScale = 2f / camera.zoom
        paint(itemCamera.combined) {
            //Draw Corpse Icon
            corpseLocation.values.forEach {
                val (x, y) = it
                val (sx, sy) = Vector2(x + 16, y - 16).mapToWindow()
                val syFix = windowHeight - sy
                val iconScale = 2f / camera.zoom
                spriteBatch.draw(corpseboximage, sx - iconScale / 2, syFix + iconScale / 2, iconScale, -iconScale,
                        0, 0, 128, 128,
                        false, true)
            }
            //Draw Airdrop Icon
            airDropLocation.values.forEach {

                val (x, y) = it
                val (sx, sy) = Vector2(x, y).mapToWindow()
                val syFix = windowHeight - sy
                val iconScale = if (camera.zoom < 1 / 14f) {
					2f / camera.zoom
				} else {
					2f / (1 / 15f)
				}
                spriteBatch.draw(airdropimage, sx - iconScale / 2, syFix + iconScale / 2, iconScale, -iconScale,
                        0, 0, 128, 128,
                        false, true)
            }
			droppedItemLocation.values
                    .forEach {
                        val (x, y) = it._1
                        val items = it._2
                        val (sx, sy) = Vector2(x, y).mapToWindow()
                        val syFix = windowHeight - sy

                        items.forEach {
                            if ((items !in uselessToFilter && items !in weaponsToFilter && items !in scopesToFilter && items !in attachToFilter && items !in level2Filter
                                            && items !in ammoToFilter && items !in healsToFilter) && items !in throwToFilter
                                    && iconScale > 20 && sx > 0 && sx < windowWidth && syFix > 0 && syFix < windowHeight) {
                                iconImages.setIcon(items)

                                draw(iconImages.icon,
                                        sx - iconScale / 2, syFix - iconScale / 2,
                                        iconScale, iconScale)
                            }
                        }
                    }

            drawMyself(tuple4(null, selfX, selfY, selfDirection))
            drawPawns(typeLocation)

        }

        /*Gdx.gl.glEnable(GL20.GL_BLEND)
        draw(Line) {
            airDropLocation.values.forEach {
                val (x, y) = it
                val airdropcoords = (Vector2(x, y))
                color = GREEN
                line(selfCoords, airdropcoords)
            }
            Gdx.gl.glDisable(GL20.GL_BLEND)
        }*/


        val zoom = camera.zoom
        Gdx.gl.glEnable(GL20.GL_BLEND)
        draw(Filled) {
            color = redZoneColor
            circle(RedZonePosition, RedZoneRadius, 100)

            color = visionColor
            circle(selfX, selfY, visionRadius, 100)

            color = pinColor
            circle(pinLocation, pinRadius * zoom, 10)

        }
        drawAttackLine(currentTime)
        Gdx.gl.glDisable(GL20.GL_BLEND)

    }

    private fun drawMyself(actorInfo: renderInfo) {
        val (actor, x, y, dir) = actorInfo
        if (actor?.netGUID == selfID) return
        val (sx, sy) = Vector2(x, y).mapToWindow()
        spriteBatch.draw(
            player,
            sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
            4.toFloat() / 2, 4.toFloat(), 4.toFloat(), playersize, playersize,
            dir * -1, 0, 0, 64, 64, true, false)
		if (toggleView != -1) {
			spriteBatch.draw(
				playersight,
				sx + 1, windowHeight - sy - 2,
				2.toFloat() / 2,
				2.toFloat() / 2,
				12.toFloat(), 2.toFloat(),
				10f, 10f,
				dir * -1, 0, 0, 512, 64, true, false)
		}
    }


    private fun drawAttackLine(currentTime: Long) {
        while (attacks.isNotEmpty()) {
            val (A, B) = attacks.poll()
            attackLineStartTime.add(Triple(A, B, currentTime))
        }
        if (attackLineStartTime.isEmpty()) return
        draw(Line) {
            val iter = attackLineStartTime.iterator()
            while (iter.hasNext()) {
                val (A, B, st) = iter.next()
                if (A == selfStateID || B == selfStateID) {
                    if (A != B) {
                        val otherGUID = playerStateToActor[if (A == selfStateID) B else A]
                        if (otherGUID == null) {
                            iter.remove()
                            continue
                        }
                        val other = actors[otherGUID]
                        if (other == null || currentTime - st > attackLineDuration) {
                            iter.remove()
                            continue
                        }
                        color = attackLineColor
                        val (xA, yA) = other.location
                        val (xB, yB) = selfCoords
                        line(xA, yA, xB, yB)
                    }
                } else {
                    val actorAID = playerStateToActor[A]
                    val actorBID = playerStateToActor[B]
                    if (actorAID == null || actorBID == null) {
                        iter.remove()
                        continue
                    }
                    val actorA = actors[actorAID]
                    val actorB = actors[actorBID]
                    if (actorA == null || actorB == null || currentTime - st > attackLineDuration) {
                        iter.remove()
                        continue
                    }
                    color = attackLineColor
                    val (xA, yA) = actorA.location
                    val (xB, yB) = actorB.location
                    line(xA, yA, xB, yB)
                }
            }
        }
    }

    private fun drawCircles() {
        Gdx.gl.glLineWidth(2f)
        draw(Line) {
            //vision circle

            color = safeZoneColor
            circle(PoisonGasWarningPosition, PoisonGasWarningRadius, 100)

            color = BLUE
            circle(SafetyZonePosition, SafetyZoneRadius, 100)

            if (PoisonGasWarningPosition.len() > 0) {
                color = safeDirectionColor
                line(selfCoords, PoisonGasWarningPosition)
            }

        }

        Gdx.gl.glLineWidth(1f)
    }


    private fun drawPawns(typeLocation: EnumMap<Archetype, MutableList<renderInfo>>) {
        val iconScale = 2f / camera.zoom
        for ((type, actorInfos) in typeLocation) {
            when (type) {
                TwoSeatBoat -> actorInfos?.forEach {
                    if (VehicleInfoToggles < 3) {
                        val (actor, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()
                        if (VehicleInfoToggles == 2) compaseFont.draw(spriteBatch, "JSKI", sx + 15, windowHeight - sy - 2)
                        spriteBatch.draw(
                                jetski,
                                sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                4.toFloat() / 2, 4.toFloat(), 4.toFloat(), iconScale / 2, iconScale / 2,
                                dir * -1, 0, 0, 64, 64, true, false
                        )
                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 40) {
                            spriteBatch.draw(
                                    parachute,
                                    sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 2 * playersize / 3, 2 * playersize / 3,
                                    dir * -1, 0, 0, 64, 64, true, false
                            )
                        }
                    }
                }
                SixSeatBoat -> actorInfos?.forEach {
                    if (VehicleInfoToggles < 3) {
                        val (actor, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()
                        if (VehicleInfoToggles == 2) compaseFont.draw(spriteBatch, "BOAT", sx + 15, windowHeight - sy - 2)
                        spriteBatch.draw(
                                boat,
                                sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                4.toFloat() / 2, 4.toFloat(), 4.toFloat(), iconScale / 2, iconScale / 2,
                                dir * -1, 0, 0, 64, 64, true, false
                        )
                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 40) {
                            spriteBatch.draw(
                                    parachute,
                                    sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 2 * playersize / 3, 2 * playersize / 3,
                                    dir * -1, 0, 0, 64, 64, true, false
                            )
                        }
                    }
                }
                TwoSeatBike -> actorInfos?.forEach {
                    if (VehicleInfoToggles < 3) {
                        val (actor, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()
                        if (VehicleInfoToggles == 2)compaseFont.draw(spriteBatch, "BIKE", sx + 15, windowHeight - sy - 2)
                        spriteBatch.draw(
                                bike,
                                sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                4.toFloat() / 2, 4.toFloat(), 4.toFloat(), iconScale / 3, iconScale / 3,
                                dir * -1, 0, 0, 64, 64, true, false
                        )
                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 40) {
                            spriteBatch.draw(
                                    parachute,
                                    sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 2 * playersize / 3, 2 * playersize / 3,
                                    dir * -1, 0, 0, 64, 64, true, false
                            )
                        }
                    }
                }
                TwoSeatCar -> actorInfos?.forEach {
                    if (VehicleInfoToggles < 3) {
                        val (actor, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()
                        if (VehicleInfoToggles == 2) compaseFont.draw(spriteBatch, "BUGGY", sx + 15, windowHeight - sy - 2)
                        spriteBatch.draw(
                                buggy,
                                sx + 2, windowHeight - sy - 2,
                                2.toFloat() / 2, 2.toFloat() / 2,
                                2.toFloat(), 2.toFloat(),
                                iconScale / 2, iconScale / 2,
                                dir * -1, 0, 0, 64, 64, false, false
                        )
                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 40) {
                            spriteBatch.draw(
                                    parachute,
                                    sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 2 * playersize / 3, 2 * playersize / 3,
                                    dir * -1, 0, 0, 64, 64, true, false
                            )
                        }
                    }
                }
                ThreeSeatCar -> actorInfos?.forEach {
                    if (VehicleInfoToggles < 3) {
                        val (actor, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()
                        if (VehicleInfoToggles == 2) compaseFont.draw(spriteBatch, "BIKE", sx + 15, windowHeight - sy - 2)
                        spriteBatch.draw(
                                bike3x,
                                sx + 2, windowHeight - sy - 2, 4.toFloat() / 2, 4.toFloat() / 2,
                                4.toFloat(), 4.toFloat(), iconScale / 2, iconScale / 2,
                                dir * -1, 0, 0, 64, 64, true, false
                        )
                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 40) {
                            spriteBatch.draw(
                                    parachute,
                                    sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 2 * playersize / 3, 2 * playersize / 3,
                                    dir * -1, 0, 0, 64, 64, true, false
                            )
                        }
                    }

                }
                FourSeatDU -> actorInfos?.forEach {
                    if (VehicleInfoToggles < 3) {
                        val (actor, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()
                        if (VehicleInfoToggles == 2) compaseFont.draw(spriteBatch, "CAR", sx + 15, windowHeight - sy - 2)
                        spriteBatch.draw(
                                vehicle,
                                sx + 2, windowHeight - sy - 2,
                                2.toFloat() / 2, 2.toFloat() / 2,
                                2.toFloat(), 2.toFloat(),
                                iconScale / 2, iconScale / 2,
                                dir * -1, 0, 0, 64, 64, false, false
                        )
                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 40) {
                            spriteBatch.draw(
                                    parachute,
                                    sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 2 * playersize / 3, 2 * playersize / 3,
                                    dir * -1, 0, 0, 64, 64, true, false
                            )
                        }
                    }

                }
                FourSeatP -> actorInfos?.forEach {
                    if (VehicleInfoToggles < 3) {
                        val (actor, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()
                        if (VehicleInfoToggles == 2)compaseFont.draw(spriteBatch, "PICKUP", sx + 15, windowHeight - sy - 2)
                        spriteBatch.draw(
                                pickup,
                                sx + 2, windowHeight - sy - 2,
                                2.toFloat() / 2, 2.toFloat() / 2,
                                2.toFloat(), 2.toFloat(),
                                iconScale / 2, iconScale / 2,
                                dir * -1, 0, 0, 64, 64, true, false
                        )
                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 40) {
                            spriteBatch.draw(
                                    parachute,
                                    sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 2 * playersize / 3, 2 * playersize / 3,
                                    dir * -1, 0, 0, 64, 64, true, false
                            )
                        }
                    }
                }
                SixSeatCar -> actorInfos?.forEach {
                    if (VehicleInfoToggles < 3) {
                        val (actor, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()
                        if (VehicleInfoToggles == 2) compaseFont.draw(spriteBatch, "VAN", sx + 15, windowHeight - sy - 2)
                        spriteBatch.draw(
                                van,
                                sx + 2, windowHeight - sy - 2,
                                2.toFloat() / 2, 2.toFloat() / 2,
                                2.toFloat(), 2.toFloat(),
                                iconScale / 2, iconScale / 2,
                                dir * -1, 0, 0, 64, 64, false, false
                        )
                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 40) {
                            spriteBatch.draw(
                                    parachute,
                                    sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 2 * playersize / 3, 2 * playersize / 3,
                                    dir * -1, 0, 0, 64, 64, true, false
                            )
                        }
                    }
                }
                Player -> actorInfos?.forEach {

                    for ((_, _) in typeLocation) {
                        val (actor, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()
                        if (isTeamMate(actor)) {
                            spriteBatch.draw(
                                    teamplayer,
                                    sx, windowHeight - sy - 2, 4.toFloat() / 2,
                                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), playersize, playersize,
                                    dir * -1, 0, 0, 64, 64, true, false)

                            if (toggleView == 1) {
                                spriteBatch.draw(
                                        playersight,
                                        sx + 1, windowHeight - sy - 2,
                                        2.toFloat() / 2,
                                        2.toFloat() / 2,
                                        12.toFloat(), 2.toFloat(),
                                        10f, 10f,
                                        dir * -1, 0, 0, 512, 64, true, false)
                            }
                        } else {
                            spriteBatch.draw(
                                    arrow,
                                    sx, windowHeight - sy - 2, 4.toFloat() / 2,
                                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), playersize, playersize,
                                    dir * -1, 0, 0, 64, 64, true, false)

                            if (toggleView == 1) {
                                spriteBatch.draw(
                                        arrowsight,
                                        sx + 1, windowHeight - sy - 2,
                                        2.toFloat() / 2,
                                        2.toFloat() / 2,
                                        12.toFloat(), 2.toFloat(),
                                        10f, 10f,
                                        dir * -1, 0, 0, 512, 64, true, false)
                            }
                        }
                    }

                }
                Parachute -> actorInfos?.forEach {
                    for ((_, _) in typeLocation) {
                        val (_, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()
                        spriteBatch.draw(
                                parachute,
                                sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                4.toFloat() / 2, 4.toFloat(), 4.toFloat(), playersize + 1f, 8f,
                                dir * -1, 0, 0, 128, 128, true, false)

                    }
                }
                Plane -> actorInfos?.forEach {
                    for ((_, _) in typeLocation) {

                        val (_, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()

                        spriteBatch.draw(
                                plane,
                                sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                4.toFloat() / 2, 5.toFloat(), 5.toFloat(), 10f, 10f,
                                dir * -1, 0, 0, 64, 64, true, false)

                    }
                }
                Grenade -> actorInfos?.forEach {
                    val (_, x, y, dir) = it
                    val (sx, sy) = Vector2(x, y).mapToWindow()
                    spriteBatch.draw(
                            grenade,
                            sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                            4.toFloat() / 2, 4.toFloat(), 4.toFloat(), playersize, playersize,
                            dir * -1, 0, 0, 16, 16, true, false)
                }

                else -> {
                    //nothing
                }
            }

        }
    }

    private fun drawPlayerNames(players: MutableList<renderInfo>?, selfX: Float, selfY: Float) {

        players?.forEach {

            val zoom = camera.zoom
            val (actor, x, y, _) = it
            if (actor != null && actor.isACharacter) {
                // actor!!

                val (sx, sy) = mapToWindow(x, y)
                val dir = Vector2(x - selfX, y - selfY)
                val distance = (dir.len() / 100).toInt()
                val playerStateGUID = actorWithPlayerState[actor.netGUID] ?: return@forEach
                val name = playerNames[playerStateGUID] ?: return@forEach
                val numKills = playerNumKills[playerStateGUID] ?: 0
                val teamNumber = teamNumbers[playerStateGUID] ?: 0
                val angle = ((dir.angle() + 90) % 360).toInt()
                val health = actorHealth[actor.netGUID] ?: 100f
                val equippedWeapons = actorHasWeapons[actor.netGUID]
                val df = DecimalFormat("###.#")
                var weapon: String? = ""

                val width = healthBarWidth * zoom
                val height = healthBarHeight * zoom
                val backgroundRadius = (playerRadius + 2000f) * zoom
                val hpY = y + backgroundRadius + height / 2


//      color = WHITE
//      rectLine(x - width / 2, y, x + width / 2, y, height+50f*zoom)
                draw(Filled) {
                    val healthWidth = (health / 100.0 * width).toFloat()
                    color = when {
                        health > 80f -> GREEN
                        health > 33f -> ORANGE
                        else -> RED
                    }
                    // rectLine(x - width / 2, hpY, x - width / 2 + healthWidth, hpY, height)

                    if (equippedWeapons != null) {
                        for (w in equippedWeapons) {
                            val a = weapons[w] ?: continue
                            val result = a.archetype.pathName.split("_")
                            weapon += "|" + result[2].substring(4) + "\n"
                        }
                    }

                    when (nameToggles) {

                        1 -> {
                            nameFont.draw(spriteBatch,
                                    "$angle°${distance}m\n" +
                                            "|N: $name\n" +
                                            "|H: ($${df.format(health)})\n" +
                                            "|K: ($numKills)\nTN.($teamNumber)\n" +
                                            "|W: $weapon",
                                    sx + 20, windowHeight - sy + 20)
                            rectLine(x - width / 2, hpY, x - width / 2 + healthWidth, hpY, height)
                        }
                        2 -> {
                            nameFont.draw(spriteBatch, "${distance}m\n" +
                                    "|N: $name\n" +
                                    "|H: ${df.format(health)}\n" +
                                    "|W: $weapon",
                                    sx + 20, windowHeight - sy + 20)
                        }
                        3 -> {
                            nameFont.draw(spriteBatch, "|N: $name\n|D: ${distance}m", sx + 20, windowHeight - sy + 20)
                        }
                        4 -> {
                            // Change color of hp
                            val healthText = health
                            when {
                                healthText > 80f -> hpgreen.draw(spriteBatch, "\n${df.format(health)}", sx + 40, windowHeight - sy + 8)
                                healthText > 33f -> hporange.draw(spriteBatch, "\n${df.format(health)}", sx + 40, windowHeight - sy + 8)
                                else -> hpred.draw(spriteBatch, "\n${df.format(health)}", sx + 40, windowHeight - sy + 8)

                            }
                            nameFont.draw(spriteBatch, "|N: $name\n|D: ${distance}m\n" +
                                    "|H:\n" +
                                    "|W: $weapon",
                                    sx + 20, windowHeight - sy + 20)

                        }
                    }
                }
            }
        }
    }


    private fun drawGrid() {
        draw(Filled) {
            val unit = gridWidth / 8
            val unit2 = unit / 10
            color = BLACK
            //thin grid
            for (i in 0..7)
                for (j in 0..9) {
                    rectLine(0f, i * unit + j * unit2, gridWidth, i * unit + j * unit2, 100f)
                    rectLine(i * unit + j * unit2, 0f, i * unit + j * unit2, gridWidth, 100f)
                }
            color = GRAY
            //thick grid
            for (i in 0..7) {
                rectLine(0f, i * unit, gridWidth, i * unit, 500f)
                rectLine(i * unit, 0f, i * unit, gridWidth, 500f)
            }
        }
    }


    private var lastPlayTime = System.currentTimeMillis()
    private fun safeZoneHint() {
        if (PoisonGasWarningPosition.len() > 0) {
            val dir = PoisonGasWarningPosition.cpy().sub(selfCoords)
            val road = dir.len() - PoisonGasWarningRadius
            if (road > 0) {
                val runningTime = (road / runSpeed).toInt()
                val (x, y) = dir.nor().scl(road).add(selfCoords).mapToWindow()
                littleFont.draw(spriteBatch, "$runningTime", x, windowHeight - y)
                val remainingTime = (TotalWarningDuration - ElapsedWarningDuration).toInt()
                if (remainingTime == 60 && runningTime > remainingTime) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastPlayTime > 10000) {
                        lastPlayTime = currentTime
                        alarmSound.play()
                    }
                }
            }
        }
    }

    private inline fun draw(type: ShapeType, draw: ShapeRenderer.() -> Unit) {
        shapeRenderer.apply {
            begin(type)
            draw()
            end()
        }
    }

    private inline fun paint(matrix: Matrix4, paint: SpriteBatch.() -> Unit) {
        spriteBatch.apply {
            projectionMatrix = matrix
            begin()
            paint()
            end()
        }
    }

    private fun ShapeRenderer.circle(loc: Vector2, radius: Float, segments: Int) {
        circle(loc.x, loc.y, radius, segments)
    }


    private fun isTeamMate(actor: Actor?): Boolean {
        if (actor != null) {
            val playerStateGUID = actorWithPlayerState[actor.netGUID]
            if (playerStateGUID != null) {
                val name = playerNames[playerStateGUID] ?: return false
                if (name in team)
                    return true
            }
        }
        return false
    }

    override fun resize(width: Int, height: Int) {
        windowWidth = width.toFloat()
        windowHeight = height.toFloat()
        camera.setToOrtho(true, windowWidth * windowToMapUnit, windowHeight * windowToMapUnit)
        itemCamera.setToOrtho(false, windowWidth, windowHeight)
        fontCamera.setToOrtho(false, windowWidth, windowHeight)
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun dispose() {
        deregister(this)
        alarmSound.dispose()
        nameFont.dispose()
        largeFont.dispose()
        littleFont.dispose()
        menuFont.dispose()
        menuFontOn.dispose()
        menuFontOFF.dispose()
        hporange.dispose()
        hpgreen.dispose()
        hpred.dispose()
        corpseboximage.dispose()
        airdropimage.dispose()
        vehicle.dispose()
        iconImages.iconSheet.dispose()
        compaseFont.dispose()
        compaseFontShadow.dispose()

        var cur = 0
        
        spriteBatch.dispose()
        shapeRenderer.dispose()
    }
}
