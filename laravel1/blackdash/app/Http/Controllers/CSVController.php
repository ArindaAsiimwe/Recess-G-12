<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\csv;

class CSVController extends Controller
{
    public function upload(Request $request){
        // $request-> validate([
        //     'csvFile'=> 'required|mimes:csv,txt|max:2048'
        // ]);

        // if($request->file('csvFile')->isValid()){
        //     $filePath = $request->file('csvFile')->store('csvFiles');
        // }

        // return redirect()->back()->with('success', 'CSV file uploaded successfully!');

        return view('pages/csv');
    }
}
