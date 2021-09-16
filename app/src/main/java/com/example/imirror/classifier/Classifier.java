package com.example.imirror.classifier;

import android.graphics.Bitmap;

import java.util.List;

public interface Classifier {
    class Recognition {
        /** A unique identifier for what has been recognized. Specific to the class, not the instance of the object. */
        private final String id;
        private final String title; // Display name for the recognition.
        private final Float confidence; // A sortable score for how good the recognition is relative to others. Higher should be better.

        public Recognition(
                final String id, final String title, final Float confidence) {
            this.id = id;
            this.title = title;
            this.confidence = confidence;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public Float getConfidence() {
            return confidence;
        }

        @Override
        public String toString() {
            String resultString = "";
//            if (id != null) {
//                resultString += "[" + id + "] ";
//            }

            if (title != null) {
                resultString += title + " ";
            }
            if (confidence != null) {
                resultString += String.format("(%.1f%%)\n", confidence * 100.0f);
            }

            resultString += "\n";
            return resultString.trim();
        }
    }

    List<Recognition> recognizeImage(Bitmap bitmap);

    void close();
}
