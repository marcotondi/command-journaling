// package dev.marcotondi.application.composite;

// import dev.marcotondi.core.api.CommandType;
// import dev.marcotondi.core.domain.CommandComposite;
// import dev.marcotondi.core.domain.CompositeDescriptor;
// import jakarta.enterprise.context.ApplicationScoped;

// /**
//  * An example composite command that demonstrates how to use CommandComposite.
//  * This command executes a 'sleep' operation followed by creating a 'todo' item.
//  */
// @ApplicationScoped
// @CommandType("SIMPLE_COMPOSITE")
// public class SampleCompositeCommand extends CommandComposite<Void> {

//     /**
//      * Constructs a new MySampleCompositeCommand.
//      *
//      * @param commandFactory The factory used to create individual commands from their descriptors.
//      */
//     public SampleCompositeCommand(CompositeDescriptor descriptors) {
//         super(descriptors);
//     }

// }
