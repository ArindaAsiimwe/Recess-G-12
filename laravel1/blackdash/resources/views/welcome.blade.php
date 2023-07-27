@extends('layouts.app')

@section('content')
    <div class="header py-7 py-lg-8">
        <div class="container" >
            <img src="{{ asset('images/WhatsApp Image 2023-06-30 at 16.57.35.jpeg') }}" alt="photo" width="200px" height="200px" border-radius="50%">
            <div class="header-body text-center mb-7">
                <div class="row justify-content-center">
                    <div class="col-lg-5 col-md-6">
                        <h1 class="text-white">{{ __('Welcome to UPRISE SACCO!') }}</h1>
                        <p class="text-lead text-light">
                         <h2 >   {{ __('Where we serve to grow') }} </h2>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </div>
@endsection
