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
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.dialog.Dialog;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.command.poll.PollModule;
import net.flectone.pulse.module.command.poll.model.NBTPoll;
import net.flectone.pulse.platform.controller.DialogController;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.Strings;

import java.util.ArrayList;
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
        Localization.Command.Poll.Modern poll = pollModule.localization(fPlayer).getModern();
        openDialog(fPlayer, poll.getInputInitial(), false, 5.0f, 1.0f, Collections.emptyList());
    }

    public void openDialog(FPlayer fPlayer, String inputValue, boolean multipleValue, float endTimeValue, float repeatTimeValue, List<String> answers) {
        Localization.Command.Poll.Modern poll = pollModule.localization(fPlayer).getModern();

        Component headerName = messagePipeline.builder(fPlayer, poll.getHeader()).build();
        DialogBody dialogBody = new PlainMessageDialogBody(new PlainMessage(Component.empty(), 10));

        Input input = new Input(INPUT_KEY, new TextInputControl(200,
                messagePipeline.builder(fPlayer, poll.getInputName()).build(),
                true,
                inputValue,
                256,
                null
        ));

        Input multiple = new Input(MULTIPLE_KEY, new BooleanInputControl(
                messagePipeline.builder(fPlayer, poll.getMultipleName()).build(),
                multipleValue,
                "true",
                "false"
        ));

        Input endTime = new Input(END_TIME_KEY, new NumberRangeInputControl(
                200,
                messagePipeline.builder(fPlayer, poll.getEndTimeName()).build(),
                "options.generic_value",
                new NumberRangeInputControl.RangeInfo(1.0f, 600.0f, endTimeValue, 1.0f)
        ));

        Input repeatTime = new Input(REPEAT_TIME_KEY, new NumberRangeInputControl(
                200,
                messagePipeline.builder(fPlayer, poll.getRepeatTimeName()).build(),
                "options.generic_value",
                new NumberRangeInputControl.RangeInfo(1.0f, 600.0f, repeatTimeValue, 1.0f)
        ));

        List<Input> inputs = new ArrayList<>(List.of(input, multiple, endTime, repeatTime));

        for (int i = 0; i < answers.size(); i++) {
            String inputAnswerName = Strings.CS.replace(poll.getInputAnswerName(), "<number>", String.valueOf(i + 1));
            Input inputAnswer = new Input(ANSWER_KEY + i, new TextInputControl(200,
                    messagePipeline.builder(fPlayer, inputAnswerName).build(),
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
        Localization.Command.Poll.Modern poll = pollModule.localization(fPlayer).getModern();

        String newAnswerButtonId = "fp_new_answer";
        ActionButton button = new ActionButton(
                new CommonButtonData(
                        messagePipeline.builder(fPlayer, poll.getNewAnswerButtonName()).build(),
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

                        List<String> answers = new ArrayList<>(nbtPoll.answers());
                        if (answers.size() < 10) {
                            answers.add(poll.getInputAnswersInitial());
                        }

                        openDialog(fPlayer, nbtPoll.input(), nbtPoll.multiple(), nbtPoll.endTime(), nbtPoll.repeatTime(), answers);
                    }
                });
    }

    private Dialog.Builder addRemoveAnswerButton(FPlayer fPlayer, Dialog.Builder builder) {
        Localization.Command.Poll.Modern poll = pollModule.localization(fPlayer).getModern();

        String newAnswerButtonId = "fp_remove_answer";
        ActionButton button = new ActionButton(
                new CommonButtonData(
                        messagePipeline.builder(fPlayer, poll.getRemoveAnswerButtonName()).build(),
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

                        List<String> answers = new ArrayList<>(nbtPoll.answers());
                        if (!answers.isEmpty()) {
                            answers.removeLast();
                        }

                        openDialog(fPlayer, nbtPoll.input(), nbtPoll.multiple(), nbtPoll.endTime(), nbtPoll.repeatTime(), answers);
                    }
                });
    }

    private Dialog.Builder addCreateButton(FPlayer fPlayer, Dialog.Builder builder) {
        String createId = "fp_create";
        ActionButton button = new ActionButton(
                new CommonButtonData(
                        messagePipeline.builder(fPlayer, pollModule.localization(fPlayer).getModern().getCreateButtonName()).build(),
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
        String inputName = nbtCompound.getStringTagValueOrDefault(INPUT_KEY, pollModule.localization(fPlayer).getModern().getInputInitial());
        boolean multiple = nbtCompound.getBooleanOr(MULTIPLE_KEY, false);
        float endTime = (float) nbtCompound.getNumberTagValueOrDefault(END_TIME_KEY, 5.0f);
        float repeatTime = (float) nbtCompound.getNumberTagValueOrDefault(REPEAT_TIME_KEY, 1.0f);

        List<String> answers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String answerValue = nbtCompound.getStringTagValueOrNull(ANSWER_KEY + i);
            if (answerValue == null) break;

            answers.add(answerValue);
        }

        return new NBTPoll(inputName, multiple, endTime, repeatTime, Collections.unmodifiableList(answers));
    }
}
