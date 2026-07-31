package com.implantiq;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {

    private List<Patient> patients;
    private OnPatientClickListener listener;

    public interface OnPatientClickListener {
        void onPatientClick(Patient patient);
    }

    public PatientAdapter(List<Patient> patients, OnPatientClickListener listener) {
        this.patients = patients;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_patient, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        Patient patient = patients.get(position);
        holder.tvName.setText(patient.getName());
        holder.tvId.setText(patient.getId());
        holder.tvDate.setText("Last prediction: " + patient.getLastPredictionDate());
        holder.tvInitials.setText(patient.getInitials());
        holder.tvGrade.setText(patient.getGrade());
        
        Context context = holder.itemView.getContext();
        if (patient.getGrade().equalsIgnoreCase("Excellent")) {
            holder.tvGrade.setTextColor(context.getResources().getColor(R.color.success));
            holder.tvGrade.setBackgroundColor(context.getResources().getColor(R.color.success_low_alpha));
        } else if (patient.getGrade().equalsIgnoreCase("Good")) {
            holder.tvGrade.setTextColor(context.getResources().getColor(R.color.accent_cyan));
            holder.tvGrade.setBackgroundColor(context.getResources().getColor(R.color.splash_circle));
        } else {
            holder.tvGrade.setTextColor(context.getResources().getColor(R.color.error));
            holder.tvGrade.setBackgroundColor(context.getResources().getColor(R.color.error_low_alpha));
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PatientDetailActivity.class);
            intent.putExtra("patient_id", patient.getId());
            intent.putExtra("patient_name", patient.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvId, tvDate, tvInitials, tvGrade;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_patient_name);
            tvId = itemView.findViewById(R.id.tv_patient_id);
            tvDate = itemView.findViewById(R.id.tv_last_prediction);
            tvInitials = itemView.findViewById(R.id.tv_avatar_initials);
            tvGrade = itemView.findViewById(R.id.tv_grade_badge);
        }
    }
}