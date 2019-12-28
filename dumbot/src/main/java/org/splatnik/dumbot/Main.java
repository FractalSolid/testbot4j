package org.splatnik.dumbot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.data.stored.ActivityBean;
import discord4j.core.object.data.stored.PresenceBean;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.VoiceChannel;
import discord4j.core.object.presence.Presence;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.voice.AudioProvider;
import org.splatnik.dumbot.dice.DieRegex;
import org.splatnik.dumbot.music.Command;
import org.splatnik.dumbot.music.LavaPlayerAudioProvider;
import org.splatnik.dumbot.music.TrackScheduler;

import java.util.*;
import java.util.function.Consumer;

public class Main {

    private static final Map<String, Command> COMMANDS = new HashMap<>();
    private static final String YUI = "https://viraljodas.com/wp-content/uploads/2019/07/aki-toyosaki-seiyuu-de-yui-en-k-on-lloro-al-conocer-la-noticia-del-incendio-en-el-estudio-de-kyoani.jpg";
    private static HashMap<Integer, String> eggs = new HashMap<>();
    private static HashMap<Integer, String> eggDescriptions = new HashMap<>();

    public static void setEggs() {
        eggs.put(1, "https://images.unsplash.com/photo-1536816579748-4ecb3f03d72a");
        eggs.put(2, "https://a57.foxnews.com/media2.foxnews.com/BrightCove/694940094001/2019/03/15/931/524/694940094001_6014490250001_6014489408001-vs.jpg?ve=1&tl=1");
        eggs.put(3, "https://www.thespruceeats.com/thmb/xcI2YxvlAT3knNg8mcmOW0oowYI=/2848x2848/smart/filters:no_upscale()/perfect-hard-boiled-eggs-995510-6-5b0d7d3efa6bcc00376a46fd.jpg");
        eggs.put(4, "https://img.freepik.com/foto-gratis/huevo-marron-sobre-fondo-blanco_33523-123.jpg");
    }

    public static void setEggDescriptions() {
        eggDescriptions.put(1, "Eggs contain the highest quality protein you can buy.");
        eggDescriptions.put(2, "To tell if an egg is raw or hard-cooked, spin it! If the egg spins easily, it is hard-cooked but if it wobbles, it is raw.");
        eggDescriptions.put(3, "Egg yolks are one of the few foods that are a naturally good source of Vitamin D.");
        eggDescriptions.put(4, "World Egg Day is celebrated every year on the second Friday in October. On World Egg Day, events are held across the world celebrating the egg.");
        eggDescriptions.put(5, "Eggs age more in one day at room temperature than in one week in the refrigerator.");
        eggDescriptions.put(6, "Egg protein has just the right mix of essential amino acids needed by humans to build tissues. It is second only to mother’s milk for human nutrition.");
    }

    static {
        COMMANDS.put("dumb", event -> event.getMessage()
                .getChannel().block()
                .createMessage("no u").block());
    }

    public static void main(String[] args) {
        setEggs();
        setEggDescriptions();
        // Creates AudioPlayer instances and translates URLs to AudioTrack instances
        final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
// This is an optimization strategy that Discord4J can utilize. It is not important to understand
        playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
// Allow playerManager to parse remote sources like YouTube links
        AudioSourceManagers.registerRemoteSources(playerManager);
// Create an AudioPlayer so Discord4J can receive audio data
        final AudioPlayer player = playerManager.createPlayer();
// We will be creating LavaPlayerAudioProvider in the next step
        AudioProvider provider = new LavaPlayerAudioProvider(player);

        DieRegex dieRegex = new DieRegex();

        COMMANDS.put("join", event -> {
            final Member member = event.getMember().orElse(null);
            if (member != null) {
                final VoiceState voiceState = member.getVoiceState().block();
                if (voiceState != null) {
                    final VoiceChannel channel = voiceState.getChannel().block();
                    if (channel != null) {
                        // join returns a VoiceConnection which would be required if we were
                        // adding disconnection features, but for now we are just ignoring it.
                        channel.join(spec -> spec.setProvider(provider)).block();
                    }
                }
            }
        });
        final TrackScheduler scheduler = new TrackScheduler(player);
        COMMANDS.put("play", event -> {
            final String content = event.getMessage().getContent().get();
            final List<String> command = Arrays.asList(content.split(" "));
            playerManager.loadItem(command.get(1), scheduler);
        });

        COMMANDS.put("roll", event -> {
            final String content = event.getMessage().getContent().get();
            final List<String> roll = Arrays.asList(content.split(" "));
            System.out.println("debug" + roll);
            String result = dieRegex.matchCheck(roll.get(1));
            event.getMessage().getChannel().block().createMessage(result).block();
        });

        COMMANDS.put("yui", event -> {
            Consumer<EmbedCreateSpec> embedCreateSpecConsumer = embedCreateSpec -> embedCreateSpec.setImage(YUI);

            event.getMessage().getChannel().block().createEmbed(embedCreateSpecConsumer).block();
        });

        COMMANDS.put("egg", event -> {


            List<Integer> keysAsArray = new ArrayList<>(eggs.keySet());
            List<Integer> keysAsArray2 = new ArrayList<>(eggDescriptions.keySet());
            Random random = new Random();
            String eggImage = eggs.get(keysAsArray.get(random.nextInt(keysAsArray.size())));
            String eggDescription = eggDescriptions.get(keysAsArray2.get(random.nextInt(keysAsArray2.size())));

            Consumer<EmbedCreateSpec> embedCreateSpecConsumer = embedCreateSpec -> embedCreateSpec.setImage(eggImage).setDescription(eggDescription);
            event.getMessage().getChannel().block().createEmbed(embedCreateSpecConsumer).block();
        });

        COMMANDS.put("addegg", event -> {
            final String contentContainer = event.getMessage().getContent().get();
            final List<String> content = Arrays.asList(contentContainer.split(" "));
            if (content.size() == 2) {
                eggs.put(eggs.size() - 1, content.get(1));
                event.getMessage().getChannel().block().createMessage("Your egg picture has been added to the collection").block();
            } else {
                event.getMessage().getChannel().block().createMessage("You absolute disgrace. That's not how you add eggs.").block();
            }
        });

        COMMANDS.put("mofo", event -> {

            String test = event.getMessage().getContent().get();
            System.out.println(test);
            PresenceBean presenceBean = new PresenceBean();
            ActivityBean activityBean = new ActivityBean();
            activityBean.setName("with your nose");
            presenceBean.setActivity(activityBean);
            Presence presence = new Presence(presenceBean);
            event.getClient().updatePresence(presence).subscribe();

        });

        final DiscordClient client = new DiscordClientBuilder(args[0]).build(); //args[0] is the bot token

        client.getEventDispatcher().on(MessageCreateEvent.class)
                .subscribe(event -> event.getMessage().getContent().ifPresent(System.out::println));

        client.getEventDispatcher().on(MessageCreateEvent.class)
                // subscribe is like block, in that it will *request* for action
                // to be done, but instead of blocking the thread, waiting for it
                // to finish, it will just execute the results asynchronously.
                .subscribe(event -> {
                    final String content = event.getMessage().getContent().orElse("");
                    for (final Map.Entry<String, Command> entry : COMMANDS.entrySet()) {
                        // "-" is the prefix for the bot commands
                        if (content.startsWith('-' + entry.getKey())) {
                            entry.getValue().execute(event);
                            break;
                        }
                    }
                });
        client.login().block();
    }
}
