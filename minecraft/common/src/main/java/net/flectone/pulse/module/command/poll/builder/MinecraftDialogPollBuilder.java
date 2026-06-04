package net.flectone.pulse.module.command.poll.builder;

import com.github.retrooper.packetevents.protocol.dialog.CommonDialogData;
import com.github.retrooper.packetevents.protocol.dialog.DialogAction;
import com.github.retrooper.packetevents.protocol.dialog.action.DynamicCustomAction;
import com.github.retrooper.packetevents.protocol.dialog.body.DialogBody;
import com.github.retrooper.packetevents.protocol.dialog.body.PlainMessage;
import com.github.retrooper.packetevents.protocol.dialog.body.PlainMessageDialogBody;
import com.github.retrooper.packetevents.protocol.dialog.button.ActionButton;
import com.github.retrooper.packetevents.protocol.dialog.button.CommonButtonData;
import com.github.retrooper.packetevents.protocol.dialog.input.BooleanInputControl;
import com.github.retrooper.packetevents.protocol.dialog.input.Input;
import com.github.retrooper.packetevents.protocol.dialog.input.NumberRangeInputControl;
import com.github.retrooper.packetevents.protocol.dialog.input.TextInputControl;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.inventory.MinecraftDialog;
import net.flectone.pulse.module.command.poll.PollModule;
import net.flectone.pulse.module.command.poll.model.NBTPoll;
import net.flectone.pulse.platform.controller.MinecraftDialogController;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.Strings;

import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftDialogPollBuilder {

    private static final String INPUT_KEY = "fp_input";
    private static final String MULTIPLE_KEY = "fp_multiple";
    private static final String END_TIME_KEY = "fp_end_time";
    private static final String REPEAT_TIME_KEY = "fp_repeat_time";
    private static final String ANSWER_KEY = "fp_answer_";

    private final PollModule pollModule;
    private final MessagePipeline messagePipeline;
    private final MinecraftDialogController dialogController;

    public void openDialog(FPlayer fPlayer) {
        Localization.Command.Poll.Modern poll = pollModule.localization(fPlayer).modern();
        openDialog(fPlayer, poll.inputInitial(), false, 5.0f, 1.0f, List.of());
    }

    public void openDialog(FPlayer fPlayer, String inputValue, boolean multipleValue, float endTimeValue, float repeatTimeValue, List<String> answers) {
        Localization.Command.Poll.Modern poll = pollModule.localization(fPlayer).modern();

        Component headerName = messagePipeline.build(MessageContext.builder()
                .sender(fPlayer)
                .message(poll.header())
                .build()
        );

        DialogBody dialogBody = new PlainMessageDialogBody(new PlainMessage(Component.empty(), 10));

        Component inputNameComponent = messagePipeline.build(MessageContext.builder()
                .sender(fPlayer)
                .message(poll.inputName())
                .build()
        );

        Input input = new Input(INPUT_KEY, new TextInputControl(200,
                inputNameComponent,
                true,
                inputValue,
                256,
                null
        ));

        Component multipleNameComponent = messagePipeline.build(MessageContext.builder()
                .sender(fPlayer)
                .message(poll.multipleName())
                .build()
        );

        Input multiple = new Input(MULTIPLE_KEY, new BooleanInputControl(
                multipleNameComponent,
                multipleValue,
                "true",
                "false"
        ));

        Component endTimeNameComponent = messagePipeline.build(MessageContext.builder()
                .sender(fPlayer)
                .message(poll.endTimeName())
                .build()
        );

        Input endTime = new Input(END_TIME_KEY, new NumberRangeInputControl(
                200,
                endTimeNameComponent,
                "options.generic_value",
                new NumberRangeInputControl.RangeInfo(1.0f, 600.0f, endTimeValue, 1.0f)
        ));

        Component repeatTimeNameComponent = messagePipeline.build(MessageContext.builder()
                .sender(fPlayer)
                .message(poll.repeatTimeName())
                .build()
        );

        Input repeatTime = new Input(REPEAT_TIME_KEY, new NumberRangeInputControl(
                200,
                repeatTimeNameComponent,
                "options.generic_value",
                new NumberRangeInputControl.RangeInfo(1.0f, 600.0f, repeatTimeValue, 1.0f)
        ));

        List<Input> inputs = new ObjectArrayList<>(List.of(input, multiple, endTime, repeatTime));

        for (int i = 0; i < answers.size(); i++) {
            Component answerNameComponent = messagePipeline.build(MessageContext.builder()
                    .sender(fPlayer)
                    .message(Strings.CS.replace(poll.inputAnswerName(), "<number>", String.valueOf(i + 1)))
                    .build()
            );

            Input inputAnswer = new Input(ANSWER_KEY + i, new TextInputControl(200,
                    answerNameComponent,
                    true,
                    answers.get(i),
                    1024,
                    new TextInputControl.MultilineOptions(5, 40)
            ));

            inputs.add(inputAnswer);
        }

        CommonDialogData commonDialogData = new CommonDialogData(
                headerName,
                null,
                true,
                false,
                DialogAction.CLOSE,
                List.of(dialogBody),
                inputs
        );

        MinecraftDialog.Builder dialogBuilder = new MinecraftDialog.Builder(commonDialogData, 2);

        dialogBuilder = addNewAnswerButton(fPlayer, dialogBuilder);
        dialogBuilder = addRemoveAnswerButton(fPlayer, dialogBuilder);
        dialogBuilder = addCreateButton(fPlayer, dialogBuilder);

        dialogController.open(fPlayer, dialogBuilder.build(), false);
    }

    private MinecraftDialog.Builder addNewAnswerButton(FPlayer fPlayer, MinecraftDialog.Builder builder) {
        Localization.Command.Poll.Modern poll = pollModule.localization(fPlayer).modern();

        String newAnswerButtonId = "fp_new_answer";
        Component buttonNameComponent = messagePipeline.build(MessageContext.builder()
                .sender(fPlayer)
                .message(poll.newAnswerButtonName())
                .build()
        );

        ActionButton button = new ActionButton(
                new CommonButtonData(
                        buttonNameComponent,
                        Component.empty(),
                        200
                ),
                new DynamicCustomAction(ResourceLocation.minecraft(newAnswerButtonId), null)
        );

        return builder
                .addButton(0, button)
                .addClickHandler(newAnswerButtonId, (_, nbt) -> {
                    if (nbt instanceof NBTCompound nbtCompound) {
                        NBTPoll nbtPoll = readPoll(fPlayer, nbtCompound);

                        List<String> answers = new ObjectArrayList<>(nbtPoll.answers());
                        if (answers.size() < 10) {
                            answers.add(poll.inputAnswersInitial());
                        }

                        openDialog(fPlayer, nbtPoll.input(), nbtPoll.multiple(), nbtPoll.endTime(), nbtPoll.repeatTime(), answers);
                    }
                });
    }

    private MinecraftDialog.Builder addRemoveAnswerButton(FPlayer fPlayer, MinecraftDialog.Builder builder) {
        Localization.Command.Poll.Modern poll = pollModule.localization(fPlayer).modern();

        String newAnswerButtonId = "fp_remove_answer";
        Component buttonNameComponent = messagePipeline.build(MessageContext.builder()
                .sender(fPlayer)
                .message(poll.removeAnswerButtonName())
                .build()
        );

        ActionButton button = new ActionButton(
                new CommonButtonData(
                        buttonNameComponent,
                        Component.empty(),
                        200
                ),
                new DynamicCustomAction(ResourceLocation.minecraft(newAnswerButtonId), null)
        );

        return builder
                .addButton(1, button)
                .addClickHandler(newAnswerButtonId, (_, nbt) -> {
                    if (nbt instanceof NBTCompound nbtCompound) {
                        NBTPoll nbtPoll = readPoll(fPlayer, nbtCompound);

                        List<String> answers = new ObjectArrayList<>(nbtPoll.answers());
                        if (!answers.isEmpty()) {
                            answers.removeLast();
                        }

                        openDialog(fPlayer, nbtPoll.input(), nbtPoll.multiple(), nbtPoll.endTime(), nbtPoll.repeatTime(), answers);
                    }
                });
    }

    private MinecraftDialog.Builder addCreateButton(FPlayer fPlayer, MinecraftDialog.Builder builder) {
        String createId = "fp_create";
        Component buttonNameComponent = messagePipeline.build(MessageContext.builder()
                .sender(fPlayer)
                .message(pollModule.localization(fPlayer).modern().createButtonName())
                .build()
        );

        ActionButton button = new ActionButton(
                new CommonButtonData(
                        buttonNameComponent,
                        Component.empty(),
                        200
                ),
                new DynamicCustomAction(ResourceLocation.minecraft(createId), null)
        );

        return builder
                .addButton(2, button)
                .addClickHandler(createId, (_, nbt) -> {
                    if (nbt instanceof NBTCompound nbtCompound) {
                        NBTPoll nbtPoll = readPoll(fPlayer, nbtCompound);

                        pollModule.createPoll(fPlayer, nbtPoll.input(), nbtPoll.multiple(), (long) (nbtPoll.endTime() * 60 * 1000L), (long) (nbtPoll.repeatTime() * 60 * 1000L), nbtPoll.answers());
                    }
                });
    }

    private NBTPoll readPoll(FPlayer fPlayer, NBTCompound nbtCompound) {
        String inputName = nbtCompound.getStringTagValueOrDefault(INPUT_KEY, pollModule.localization(fPlayer).modern().inputInitial());
        boolean multiple = nbtCompound.getBooleanOr(MULTIPLE_KEY, false);
        float endTime = (float) nbtCompound.getNumberTagValueOrDefault(END_TIME_KEY, 5.0f);
        float repeatTime = (float) nbtCompound.getNumberTagValueOrDefault(REPEAT_TIME_KEY, 1.0f);

        List<String> answers = new ObjectArrayList<>();
        for (int i = 0; i < 10; i++) {
            String answerValue = nbtCompound.getStringTagValueOrNull(ANSWER_KEY + i);
            if (answerValue == null) break;

            answers.add(answerValue);
        }

        return new NBTPoll(inputName, multiple, endTime, repeatTime, List.copyOf(answers));
    }
}