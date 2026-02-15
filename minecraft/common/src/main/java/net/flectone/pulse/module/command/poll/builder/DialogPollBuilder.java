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
import net.flectone.pulse.model.dialog.Dialog;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.command.poll.PollModule;
import net.flectone.pulse.module.command.poll.model.NBTPoll;
import net.flectone.pulse.platform.controller.DialogController;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.Strings;

import java.util.Collections;
import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DialogPollBuilder {

    private static final String INPUT_KEY = "fp_input";
    private static final String MULTIPLE_KEY = "fp_multiple";
    private static final String END_TIME_KEY = "fp_end_time";
    private static final String REPEAT_TIME_KEY = "fp_repeat_time";
    private static final String ANSWER_KEY = "fp_answer_";

    private final PollModule pollModule;
    private final MessagePipeline messagePipeline;
    private final DialogController dialogController;

    public void openDialog(FPlayer fPlayer) {
        Localization.Command.Poll.Modern poll = pollModule.localization(fPlayer).modern();
        openDialog(fPlayer, poll.inputInitial(), false, 5.0f, 1.0f, Collections.emptyList());
    }

    public void openDialog(FPlayer fPlayer, String inputValue, boolean multipleValue, float endTimeValue, float repeatTimeValue, List<String> answers) {
        Localization.Command.Poll.Modern poll = pollModule.localization(fPlayer).modern();

        MessageContext headerContext = messagePipeline.createContext(fPlayer, poll.header());
        Component headerName = messagePipeline.build(headerContext);

        DialogBody dialogBody = new PlainMessageDialogBody(new PlainMessage(Component.empty(), 10));

        MessageContext inputNameContext = messagePipeline.createContext(fPlayer, poll.inputName());
        Component inputNameComponent = messagePipeline.build(inputNameContext);

        Input input = new Input(INPUT_KEY, new TextInputControl(200,
                inputNameComponent,
                true,
                inputValue,
                256,
                null
        ));

        MessageContext multipleNameContext = messagePipeline.createContext(fPlayer, poll.multipleName());
        Component multipleNameComponent = messagePipeline.build(multipleNameContext);

        Input multiple = new Input(MULTIPLE_KEY, new BooleanInputControl(
                multipleNameComponent,
                multipleValue,
                "true",
                "false"
        ));

        MessageContext endTimeNameContext = messagePipeline.createContext(fPlayer, poll.endTimeName());
        Component endTimeNameComponent = messagePipeline.build(endTimeNameContext);

        Input endTime = new Input(END_TIME_KEY, new NumberRangeInputControl(
                200,
                endTimeNameComponent,
                "options.generic_value",
                new NumberRangeInputControl.RangeInfo(1.0f, 600.0f, endTimeValue, 1.0f)
        ));

        MessageContext repeatTimeNameContext = messagePipeline.createContext(fPlayer, poll.repeatTimeName());
        Component repeatTimeNameComponent = messagePipeline.build(repeatTimeNameContext);

        Input repeatTime = new Input(REPEAT_TIME_KEY, new NumberRangeInputControl(
                200,
                repeatTimeNameComponent,
                "options.generic_value",
                new NumberRangeInputControl.RangeInfo(1.0f, 600.0f, repeatTimeValue, 1.0f)
        ));

        List<Input> inputs = new ObjectArrayList<>(List.of(input, multiple, endTime, repeatTime));

        for (int i = 0; i < answers.size(); i++) {
            String inputAnswerName = Strings.CS.replace(poll.inputAnswerName(), "<number>", String.valueOf(i + 1));
            MessageContext answerNameContext = messagePipeline.createContext(fPlayer, inputAnswerName);
            Component answerNameComponent = messagePipeline.build(answerNameContext);

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

        Dialog.Builder dialogBuilder = new Dialog.Builder(commonDialogData, 2);

        dialogBuilder = addNewAnswerButton(fPlayer, dialogBuilder);
        dialogBuilder = addRemoveAnswerButton(fPlayer, dialogBuilder);
        dialogBuilder = addCreateButton(fPlayer, dialogBuilder);

        dialogController.open(fPlayer, dialogBuilder.build(), false);
    }

    private Dialog.Builder addNewAnswerButton(FPlayer fPlayer, Dialog.Builder builder) {
        Localization.Command.Poll.Modern poll = pollModule.localization(fPlayer).modern();

        String newAnswerButtonId = "fp_new_answer";
        MessageContext buttonNameContext = messagePipeline.createContext(fPlayer, poll.newAnswerButtonName());
        Component buttonNameComponent = messagePipeline.build(buttonNameContext);

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
                .addClickHandler(newAnswerButtonId, (dialog, nbt) -> {
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

    private Dialog.Builder addRemoveAnswerButton(FPlayer fPlayer, Dialog.Builder builder) {
        Localization.Command.Poll.Modern poll = pollModule.localization(fPlayer).modern();

        String newAnswerButtonId = "fp_remove_answer";
        MessageContext buttonNameContext = messagePipeline.createContext(fPlayer, poll.removeAnswerButtonName());
        Component buttonNameComponent = messagePipeline.build(buttonNameContext);

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
                .addClickHandler(newAnswerButtonId, (dialog, nbt) -> {
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

    private Dialog.Builder addCreateButton(FPlayer fPlayer, Dialog.Builder builder) {
        String createId = "fp_create";
        MessageContext buttonNameContext = messagePipeline.createContext(fPlayer, pollModule.localization(fPlayer).modern().createButtonName());
        Component buttonNameComponent = messagePipeline.build(buttonNameContext);

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
                .addClickHandler(createId, (dialog, nbt) -> {
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

        return new NBTPoll(inputName, multiple, endTime, repeatTime, Collections.unmodifiableList(answers));
    }
}