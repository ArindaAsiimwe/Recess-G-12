<?php

namespace App\Http\Controllers;

use App\Models\member;
use Illuminate\Http\Request;

class MemberController extends Controller
{
    /**
     * Display a listing of the resource.
     */
    public function index()
    {
        $member=member::all();
            return view('admin.members.member',['members'=>$member]);
    }

    /**
     * Show the form for creating a new resource.
     */
    public function create()
    {
        return view('admin.members.addmember');
    }

    /**
     * Store a newly created resource in storage.
     */
    public function store(Request $request)
    {
        $validatedData = $request->validate([
            'memberID' => 'required|string|max:255',
            'username' => 'required|string|max:255',
            'password' => 'required|string|min:6',
            'email' => 'required|email|unique:members',
            'phone_number' => 'required|string|max:20',
            // Add other validation rules for your form fields
        ]);

        // Create a new Member instance and set its attributes
        $member = new Member();
        $member->memberID = $validatedData['memberID'];
        $member->username = $validatedData['username'];
        $member->password = $validatedData['password'];
         // Hash the password for security $member->password = bcrypt($validatedData['password']); 
        $member->email = $validatedData['email'];
        $member->phone_number = $validatedData['phone_number'];
        // Set other attributes as needed

        // Save the new member to the database
        $member->save();

        // Redirect to the members list or any other page as needed
        return redirect()->route('member.index');
    }

    /**
     * Display the specified resource.
     */
    public function show(member $member)
    {
        //
    }

    /**
     * Show the form for editing the specified resource.
     */
    public function edit(member $member)
    {
        //
    }

    /**
     * Update the specified resource in storage.
     */
    public function update(Request $request, member $member)
    {
        //
    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy(member $member)
    {
        //
    }
}
